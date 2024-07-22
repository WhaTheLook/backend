package com.example.demo.Service;

import com.example.demo.DTO.CommentRequest;
import com.example.demo.DTO.CommentUpdateRequest;
import com.example.demo.DTO.CommentResponse;
import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Post;
import com.example.demo.Entity.User;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addComment(CommentRequest commentRequest) {
        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findById(commentRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Comment parent = commentRequest.getParentId() != null ? commentRepository.findById(commentRequest.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found")) : null;

        int depth = parent != null ? parent.getDepth() + 1 : 0;
        int order = parent != null ? parent.getOrder() + 1 : (int) commentRepository.countByPost(post) + 1;

        // 새로운 댓글의 order보다 큰 order를 가진 댓글들의 order를 +1 씩 증가
        List<Comment> commentsToUpdate = commentRepository.findByPostAndOrderGreaterThanEqual(post, order);
        for (Comment comment : commentsToUpdate) {
            comment.setOrder(comment.getOrder() + 1);
            commentRepository.save(comment);
        }

        Comment comment = Comment.builder()
                .text(commentRequest.getText())
                .depth(depth)
                .order(order)
                .date(new Date())
                .deleteYN(false)
                .post(post)
                .user(user)
                .parent(parent)
                .build();

        commentRepository.save(comment);
    }

    public List<CommentResponse> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        // 댓글 목록을 가져온다.
        List<Comment> comments = commentRepository.findAllByPostAndDeleteYNOrderByOrderAsc(post, false);

        // 댓글을 CommentResponse로 변환
        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .author(comment.getUser().getName()) // 사용자 이름
                        .content(comment.getText()) // 댓글 내용
                        .date(TimeUtil.getRelativeTime(comment.getDate())) // 상대적 시간
                        .depth(comment.getDepth()) // 댓글 깊이
                        .order(comment.getOrder()) // 댓글 순서
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        comment.setDeleteYN(true);
        commentRepository.save(comment);
    }

    @Transactional
    public void updateComment(CommentUpdateRequest commentRequest) {
        // 댓글 ID로 댓글 조회
        Comment comment = commentRepository.findById(commentRequest.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // 댓글의 내용 업데이트
        comment.setText(comment.getText());
        // 댓글 저장
        commentRepository.save(comment);
    }


}
