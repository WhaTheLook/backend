package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final LikeRepository likeRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentService commentService;
    private final S3Service s3Service;
    private final UserService userService;

    public void savePost(PostRequest postDto) throws IOException {
        System.out.println(postDto);
        Optional<User> optionalUser = userRepository.findByKakaoId(postDto.getKakaoId());
        User author = optionalUser.orElseThrow(() ->
                new IllegalArgumentException("Invalid kakaoId: " + postDto.getKakaoId())
        );
        Post post = Post.builder()
                .author(author)
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .category(postDto.getCategory())
                .date(new Date())
                .deleteYN(false)
                .build();

        Post savedPost = postRepository.save(post);

        List<Photo> photoEntities = new ArrayList<>();
        if(postDto.getPhotos().size() != 0) {
            for (MultipartFile photo : postDto.getPhotos()) {
                String url = s3Service.uploadFile(photo);

                Photo photoEntity = new Photo();
                photoEntity.setUrl(url);
                photoEntity.setDate(new Date());
                photoEntity.setPost(savedPost);
                photoEntities.add(photoEntity);
            }
            photoRepository.saveAll(photoEntities);
        }

        List<Hashtag> hashtags = new ArrayList<>();
        if(postDto.getHashtags().size() != 0) {
            for (String name : postDto.getHashtags()) {
                Hashtag hashtag = new Hashtag();
                hashtag.setName(name);
                hashtag.setPost(savedPost);
                hashtags.add(hashtag);
            }
            hashtagRepository.saveAll(hashtags);
        }
//
//        savedPost.setPhotos(photoEntities);
//        savedPost.setHashtags(hashtags);
//
//        postRepository.save(savedPost);
    }

    // 게시판 목록
    public Slice<PostResponse> getPostList(int size, String category, String sortBy, Long lastPostId, String kakaoId) {

        // 정렬 방법
        Sort sort = Sort.by(Sort.Order.desc("id"));
//        if(sortBy.equals("popular")){
//            // 인기순 (인기 동일시 최신순)
//            sort = Sort.by(Sort.Order.desc("likes"), Sort.Order.desc("date"));
//        }else{
//            // 최신순
//            sort = Sort.by(Sort.Order.desc("id"));
//        }

        Pageable pageable = PageRequest.of(0, size, sort);
//        Page<Post> posts = postRepository.findByDeleteYNFalseAndCategory(pageable, category);
        Slice<Post> posts;
        if (lastPostId == null) {
            posts = postRepository.findByDeleteYNFalseAndCategory(pageable, category);
        } else {
            posts = postRepository.findByDeleteYNFalseAndCategoryAndIdLessThan(category, lastPostId, pageable);
        }

        System.out.println(posts.getContent());


        return posts.map(post -> convertToPostResponse(post,kakaoId));
    }

    // 특정 회원의 게시판 목록
    public Slice<PostResponse> userPostList(int size, String sortBy, Long lastPostId, String kakaoId) {

        // 정렬 방법
        Sort sort = Sort.by(Sort.Order.desc("id"));

        // User 객체 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(0, size, sort);

        Slice<Post> posts;
        if (lastPostId == null) {
            posts = postRepository.findByDeleteYNFalseAndAuthor(pageable, user);
        } else {
            posts = postRepository.findByDeleteYNFalseAndAuthorAndIdLessThan(user, lastPostId, pageable);
        }

        return posts.map(post -> convertToPostResponse(post,kakaoId));
    }

    // 특정 회원의 좋아요 게시판 목록
    public Slice<PostResponse> userLikePostList(int size, String sortBy, Long lastPostId, String kakaoId) {

        // 정렬 방법
        Sort sort = Sort.by(Sort.Order.desc("id"));

        // User 객체 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(0, size, sort);

        Slice<Post> posts;
        if (lastPostId == null) {
            posts = postRepository.findByUserLikes(user, null, pageable);
        } else {
            posts = postRepository.findByUserLikes(user, lastPostId, pageable);
        }

        return posts.map(post -> convertToPostResponse(post,kakaoId));
    }


    // 특정 회원이 작성한 댓글을 기반으로 게시글 목록을 조회, 무한 스크롤을 위한 lastPostId 사용
    public Slice<PostResponse> getPostsByUserComments(String kakaoId, Long lastPostId, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Order.desc("id")));

        // 특정 회원이 작성한 댓글들이 달린 게시글을 조회
        Slice<Post> posts = commentRepository.findDistinctPostsByAuthorKakaoIdAndPostIdLessThanAndDeleteYNFalse(kakaoId, lastPostId, pageable);

        // 게시글 목록을 PostResponse로 변환해서 반환
        return posts.map(post -> convertToPostResponse(post,kakaoId));
    }


    // 해시태그 검색 목록
    public SearchResponse getPostsByHashtag(int size, String sortBy, Long lastPostId, String hashtagName, String kakaoId) {
        // Pageable 객체 생성
        Sort sort = Sort.by(Sort.Order.desc("id"));

        Pageable pageable = PageRequest.of(0, size, sort);

        // 해시태그를 포함하는 게시물 목록 조회
        Slice<Post> posts;
        long totalPosts = 0;
        if (lastPostId == null) {
            posts = postRepository.findByHashtagName(hashtagName, null, pageable);
            totalPosts = postRepository.countByDeleteYNFalseAndHashtagsContaining(hashtagName);
        } else {
            posts = postRepository.findByHashtagName(hashtagName, lastPostId, pageable);
        }
        Slice<PostResponse> postResponses = posts.map(post -> convertToPostResponse(post,kakaoId));
        
        return SearchResponse.builder().total(totalPosts).posts(postResponses).build();
    }

    public PostDetailResponse getPost(Long postId, String kakaoId) {
        Post post = postRepository.findByIdAndDeleteYNFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Slice<CommentResponse> comments = commentService.getComments(postId, null, kakaoId);
        Long commentCount = commentService.commentCount(post);

        Author author = null;
        boolean likeYN = false;


        if (kakaoId != null) {
            System.out.println("enter");
            System.out.println(kakaoId);
            likeYN = likeRepository.existsByPostAndUserKakaoId(post, kakaoId);
        }

        System.out.println("들어갔나?");

        if(post.getAuthor() != null){
            author = Author.builder()
                    .kakaoId(post.getAuthor().getKakaoId())
                    .name(post.getAuthor().getName())
                    .profileImage(post.getAuthor().getProfileImage())
                    .build();

        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        // 채택된 댓글 조회
        Comment acceptedComment = commentRepository.findByPostAndAcceptTrueAndDeleteYNFalse(post)
                .orElse(null);  // 채택된 댓글이 없을 경우 null

        CommentResponse acceptResponse = null;

        // 채택된 댓글이 있을 경우 Response로 변환
        if (acceptedComment != null) {
            acceptResponse = commentService.convertToCommentResponse(acceptedComment);
        }

        return PostDetailResponse.builder()
                .id(post.getId())
                .author(author)
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .date(format.format(post.getDate()))
                .deleteYN(post.getDeleteYN())
                .likeCount(post.getLikes()) // 좋아요 수 계산
                .commentCount(commentCount)
                .hashtags(post.getHashtags().stream()
                        .map(Hashtag::getName) // 해시태그 이름으로 변환
                        .collect(Collectors.toList()))
                .photoUrls(post.getPhotos().stream()
                        .map(Photo::getUrl) // 사진 URL로 변환
                        .collect(Collectors.toList()))
                .comments(comments.toList())
                .likeYN(likeYN)
                .accept(acceptResponse)
                .build();
    }

    @Transactional
    public Post updatePost(PostUpdateRequest postUpdateRequest) throws IOException {
        // 기존 게시물 찾기
        Post post = postRepository.findById(postUpdateRequest.getId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Optional<User> optionalUser = userRepository.findByKakaoId(postUpdateRequest.getAuthor());
        User author = optionalUser.orElseThrow(() ->
                new IllegalArgumentException("Invalid kakaoId: " + postUpdateRequest.getAuthor())
        );

        System.out.println("확인");
        // 게시물 정보 업데이트
        if (postUpdateRequest.getAuthor() != null) {
            post.setAuthor(author);
            System.out.println("유저 변경");
        }
        if (postUpdateRequest.getTitle() != null) {
            post.setTitle(postUpdateRequest.getTitle());
            System.out.println("타이틀 변경");
        }
        if (postUpdateRequest.getContent() != null) {
            post.setContent(postUpdateRequest.getContent());
            System.out.println("내용 변경");
        }
        if (postUpdateRequest.getCategory() != null) {
            post.setCategory(postUpdateRequest.getCategory());
            System.out.println("카테고리 변경");
        }

//        post.setDate(new Date());  // 마지막 수정일을 현재 시간으로 업데이트
//        System.out.println("날짜 변경");

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
            }System.out.println("해시태그 변경");
        }

        // 사진 처리
        if (postUpdateRequest.getPhotos() != null && !postUpdateRequest.getPhotos().isEmpty()) {

            // 게시물에 대한 기존 사진 URL 목록
            List<String> existingPhotoUrls = photoRepository.findByPost(post).stream()
                    .map(Photo::getUrl)
                    .collect(Collectors.toList());

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

            List<Photo> photosToSave = newPhotoUrls.stream()
                    .map(url -> Photo.builder()
                            .url(url)
                            .date(new Date())
                            .deleteYN(false)
                            .post(post)
                            .build())
                    .collect(Collectors.toList());

            photoRepository.saveAll(photosToSave);
            System.out.println("사진 변경 후 저장 완료");

            // 기존 사진 삭제
            existingPhotoUrls.stream().forEach(file -> {
                try{
                    System.out.println("삭제하러 들어오기는 함");
                    System.out.println("file : " + file);
                    s3Service.deleteFile(file);
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            });
            photoRepository.deleteByPostAndUrlIn(post, existingPhotoUrls);
            System.out.println("기존 사진 삭제 완료");

            post.toBuilder().photos(photosToSave);
        }

        // 게시물 저장
        System.out.println("데이터 최종 저장");
        return postRepository.save(post);
//        return convertToPostResponse(post, postUpdateRequest.getAuthor());
    }

    @Transactional
    public void deletePost(Long postId, String kakaoId) {
        // 게시판 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        // User 객체 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if(post.getAuthor().getKakaoId().equals(kakaoId)){
            // 게시판 삭제 여부를 true로 설정
            post.setDeleteYN(true);
            // 게시판 저장 (deleteYN 필드가 true로 업데이트됨)
            postRepository.save(post);
            // 게시물에 달린 댓글들을 조회하고 삭제
            List<Comment> comments = commentRepository.findByPost(post);
            for (Comment comment : comments) {
                commentService.deleteComment(comment.getId(), kakaoId);  // 댓글 삭제 호출
            }
        }else{
            // 작성자와 요청자가 다를 경우 예외 던지기
            throw new IllegalArgumentException("You are not the author of this post");
        }


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

    public PostResponse convertToPostResponse(Post post, String kakaoId) {
        System.out.println(post.getAuthor());
        Author author = null;
        boolean likeYN = false;
        if(post.getAuthor() != null){
            author = Author.builder()
                    .kakaoId(post.getAuthor().getKakaoId())
                    .name(post.getAuthor().getName())
                    .profileImage(post.getAuthor().getProfileImage())
                    .build();

        }

        if (kakaoId != null) {
            likeYN = likeRepository.existsByPostAndUserKakaoId(post, kakaoId);

        }

//        // 원하는 형식 지정
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime localDateTime = post.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return PostResponse.builder()
                .id(post.getId())
                .author(author)
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
//                .date(TimeUtil.getRelativeTime(post.getDate())) // 상대적 시간 변환
                .date(format.format(post.getDate()))
                .likeCount(post.getLikes()) // 좋아요 수 계산
                .commentCount(commentService.commentCount(post))
                .hashtags(post.getHashtags().stream()
                        .map(Hashtag::getName) // 해시태그 이름으로 변환
                        .collect(Collectors.toList()))
                .photoUrls(post.getPhotos().stream()
                        .map(Photo::getUrl) // 사진 URL로 변환
                        .collect(Collectors.toList()))
                .likeYN(likeYN)
                .build();
    }
}
