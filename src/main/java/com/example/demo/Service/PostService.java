package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Entity.Hashtag;
import com.example.demo.Entity.Photo;
import com.example.demo.Entity.Post;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.HashtagRepository;
import com.example.demo.Repository.PhotoRepository;
import com.example.demo.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentService commentService;
    private final S3Service s3Service;

    public void savePost(PostRequest postDto) throws IOException {
        Post post = Post.builder()
                .author(postDto.getAuthor())
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .category(postDto.getCategory())
                .date(new Date())
                .deleteYN(false)
                .build();

        Post savedPost = postRepository.save(post);

        List<Photo> photoEntities = new ArrayList<>();
        for (MultipartFile photo : postDto.getPhotos()) {
            String url = s3Service.uploadFile(photo);

            Photo photoEntity = new Photo();
            photoEntity.setUrl(url);
            photoEntity.setDate(new Date());
            photoEntity.setPost(savedPost);
            photoEntities.add(photoEntity);
        }
        photoRepository.saveAll(photoEntities);

        List<Hashtag> hashtags = new ArrayList<>();
        for (String name : postDto.getHashtags()) {
            Hashtag hashtag = new Hashtag();
            hashtag.setName(name);
            hashtag.setPost(savedPost);
            hashtags.add(hashtag);
        }
        hashtagRepository.saveAll(hashtags);

        savedPost.setPhotos(photoEntities);
        savedPost.setHashtags(hashtags);

        postRepository.save(savedPost);
    }

    public Page<PostResponse> getPostList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByDeleteYNFalse(pageable);

        return posts.map(this::convertToPostResponse);
    }

    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        List<CommentResponse> comments = commentService.getComments(postId);

        return PostDetailResponse.builder()
                .id(post.getId())
                .author(post.getAuthor())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .date(TimeUtil.getRelativeTime(post.getDate())) // 상대적 시간 변환
                .deleteYN(post.getDeleteYN())
                .likeCount(post.getLikes()) // 좋아요 수 계산
                .hashtags(post.getHashtags().stream()
                        .map(Hashtag::getName) // 해시태그 이름으로 변환
                        .collect(Collectors.toList()))
                .photoUrls(post.getPhotos().stream()
                        .map(Photo::getUrl) // 사진 URL로 변환
                        .collect(Collectors.toList()))
                .comments(comments)
                .build();
    }

    @Transactional
    public Post updatePost(PostUpdateRequest postUpdateRequest) throws IOException {
        // 기존 게시물 찾기
        Post post = postRepository.findById(postUpdateRequest.getId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 게시물 정보 업데이트
        if (postUpdateRequest.getAuthor() != null) {
            post.setAuthor(postUpdateRequest.getAuthor());
        }
        if (postUpdateRequest.getTitle() != null) {
            post.setTitle(postUpdateRequest.getTitle());
        }
        if (postUpdateRequest.getContent() != null) {
            post.setContent(postUpdateRequest.getContent());
        }
        if (postUpdateRequest.getCategory() != null) {
            post.setCategory(postUpdateRequest.getCategory());
        }
        post.setDate(new Date());  // 마지막 수정일을 현재 시간으로 업데이트

        // 해시태그 처리
        if (postUpdateRequest.getHashtags() != null) {
            hashtagRepository.deleteByPost(post);

            // 새로운 해시태그 저장
            for (String hashtagName : postUpdateRequest.getHashtags()) {
                Hashtag newHashtag = Hashtag.builder()
                        .name(hashtagName)
                        .post(post)
                        .build();
                hashtagRepository.save(newHashtag);
            }
        }

        // 사진 처리
        if (postUpdateRequest.getPhotos() != null && !postUpdateRequest.getPhotos().isEmpty()) {
            // 프론트에서 제공한 삭제할 사진 URL 목록
            List<String> photosToDelete = postUpdateRequest.getDeleteUrl(); // 프론트에서 제공된 삭제할 사진 URL 목록

            // 게시물에 대한 기존 사진 URL 목록
            List<String> existingPhotoUrls = photoRepository.findByPost(post).stream()
                    .map(Photo::getUrl)
                    .collect(Collectors.toList());

            // 삭제할 사진 URL 목록을 기준으로 사진 삭제
            photoRepository.deleteByPostAndUrlIn(post, photosToDelete);

            // 새로운 사진 업로드 및 저장
            List<String> newPhotoUrls = postUpdateRequest.getPhotos().stream()
                    .map(file -> {
                        try {
                            return s3Service.uploadFile(file); // 사진 파일을 S3에 업로드하고 URL 반환
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to upload photo", e);
                        }
                    })
                    .collect(Collectors.toList());

            // 기존 사진 중 삭제되지 않은 사진을 유지하기 위해
            List<Photo> photosToSave = newPhotoUrls.stream()
                    .filter(url -> !existingPhotoUrls.contains(url)) // 새로운 사진 중 기존에 없는 사진만 필터링
                    .map(url -> Photo.builder()
                            .url(url)
                            .date(new Date())
                            .deleteYN(false)
                            .post(post)
                            .build())
                    .collect(Collectors.toList());

            photoRepository.saveAll(photosToSave);
        }

        // 게시물 저장
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        // 게시판 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 게시판 삭제 여부를 true로 설정
        post.setDeleteYN(true);

        // 게시판 저장 (deleteYN 필드가 true로 업데이트됨)
        postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long postId, PostUpdateRequest updateRequest) {
        // 게시물 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 게시물 수정
        post.setTitle(updateRequest.getTitle());
        post.setContent(updateRequest.getContent());
        post.setCategory(updateRequest.getCategory());
        // 삭제 여부는 수정할 수 없는 경우가 많으므로 설정하지 않음

        // 수정된 게시물 저장
        return postRepository.save(post);
    }

    private PostResponse convertToPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .author(post.getAuthor())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .date(TimeUtil.getRelativeTime(post.getDate())) // 상대적 시간 변환
                .likeCount(post.getLikes()) // 좋아요 수 계산
                .hashtags(post.getHashtags().stream()
                        .map(Hashtag::getName) // 해시태그 이름으로 변환
                        .collect(Collectors.toList()))
                .photoUrls(post.getPhotos().stream()
                        .map(Photo::getUrl) // 사진 URL로 변환
                        .collect(Collectors.toList()))
                .build();
    }
}
