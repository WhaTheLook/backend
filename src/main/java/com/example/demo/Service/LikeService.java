package com.example.demo.Service;

import com.example.demo.DTO.LikeRequest;
import com.example.demo.Entity.Like;
import com.example.demo.Entity.Post;
import com.example.demo.Entity.User;
import com.example.demo.Repository.LikeRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시판에 좋아요 추가 또는 제거 메서드
     * @param likeRequest 좋아요 요청 객체
     */
    @Transactional
    public void toggleLike(LikeRequest likeRequest) {
        // 게시판과 사용자 조회
        Post post = postRepository.findById(likeRequest.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findById(likeRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 사용자와 게시판의 좋아요를 확인
        Like existingLike = likeRepository.findByPostAndUser(post, user);

        if (existingLike != null) {
            // 이미 좋아요가 있는 경우, 좋아요 제거
            likeRepository.delete(existingLike);
        } else {
            // 좋아요가 없는 경우, 새로운 좋아요 추가
            Like newLike = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(newLike);
        }

        // 게시판의 좋아요 개수 업데이트
        updateLikeCount(post);
    }

    private void updateLikeCount(Post post) {
        // 게시판의 현재 좋아요 개수 조회
        long likeCount = likeRepository.countByPost(post);

        // 게시판의 좋아요 개수 업데이트
        post.builder()
                .likes(likeCount)
                .build();
        postRepository.save(post);
    }
}