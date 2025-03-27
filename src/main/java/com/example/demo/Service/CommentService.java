package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Post;
import com.example.demo.Entity.User;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

//    @Transactional
//    public void addComment(CommentRequest commentRequest) {
//        Post post = postRepository.findById(commentRequest.getPostId())
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//        User user = userRepository.findById(commentRequest.getUserId())
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//        Comment parent = commentRequest.getParentId() != null ? commentRepository.findById(commentRequest.getParentId())
//                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found")) : null;
//
//        int depth = parent != null ? parent.getDepth() + 1 : 0;
//        int order = parent != null ? parent.getOrder() + 1 : (int) commentRepository.countByPost(post) + 1;
//
//        // 새로운 댓글의 order보다 큰 order를 가진 댓글들의 order를 +1 씩 증가
//        List<Comment> commentsToUpdate = commentRepository.findByPostAndOrderGreaterThanEqual(post, order);
//        for (Comment comment : commentsToUpdate) {
//            comment.setOrder(comment.getOrder() + 1);
//            commentRepository.save(comment);
//        }
//
//        Comment comment = Comment.builder()
//                .text(commentRequest.getText())
//                .depth(depth)
//                .order(order)
//                .date(new Date())
//                .deleteYN(false)
//                .post(post)
//                .user(user)
//                .parent(parent)
//                .build();
//
//        commentRepository.save(comment);
//    }

//    public List<CommentResponse> getComments(Long postId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//        // 댓글 목록을 가져온다.
//        List<Comment> comments = commentRepository.findAllByPostAndDeleteYNOrderByOrderAsc(post, false);
//
//        // 댓글을 CommentResponse로 변환
//        return comments.stream()
//                .map(comment -> CommentResponse.builder()
//                        .id(comment.getId())
//                        .author(comment.getUser().getName()) // 사용자 이름
//                        .text(comment.getText()) // 댓글 내용
//                        .date(TimeUtil.getRelativeTime(comment.getDate())) // 상대적 시간
//                        .depth(comment.getDepth()) // 댓글 깊이
//                        .order(comment.getOrder()) // 댓글 순서
//                        .build())
//                .collect(Collectors.toList());
//    }

//    @Transactional
//    public void deleteComment(Long commentId) {
//        Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
//
//        comment.setDeleteYN(true);
//        commentRepository.save(comment);
//    }
//
//    @Transactional
//    public void updateComment(CommentUpdateRequest commentRequest) {
//        // 댓글 ID로 댓글 조회
//        Comment comment = commentRepository.findById(commentRequest.getCommentId())
//                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
//
//        // 댓글의 내용 업데이트
//        comment.setText(comment.getText());
//        // 댓글 저장
//        commentRepository.save(comment);
//    }




    // 댓글 목록
    public Slice<CommentResponse> getComments(Long postId, Long lastCommentId, String kakaoId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Sort userSort  = Sort.by(Sort.Order.desc("id"));;
        Sort otherSort  = Sort.by(Sort.Order.asc("id"));;


        Pageable userPageable = PageRequest.of(0, 10, userSort);
        Pageable otherPageable = PageRequest.of(0, 10,otherSort);
        Slice<Comment> comments;


        Slice<Comment> userComments = new SliceImpl<>(Collections.emptyList(), userPageable, false);
        Slice<Comment> otherComments = new SliceImpl<>(Collections.emptyList(), otherPageable, false);

        User kakaoUser = null;

        boolean isMyComment = false;

        if(lastCommentId != null){
            Comment lastComment = commentRepository.findById(lastCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("Last comment not found"));
            isMyComment = lastComment.getUser().getKakaoId().equals(kakaoId);
        }


        // 카카오 아이디가 존재하는지 확인
        if (kakaoId != null) {
            kakaoUser = userRepository.findByKakaoId(kakaoId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        System.out.println(kakaoUser);
//        System.out.println(kakaoUser.getKakaoId());
        // 1. 해당 유저의 댓글을 먼저 조회
        if (kakaoUser != null) {
            System.out.println("유저 댓글 먼저 조회");
            if (lastCommentId == null) {
                System.out.println("댓글 첫 조회");
                userComments = commentRepository.findByPostAndUserAndParentIsNullAndDeleteYNFalse(post, kakaoUser, userPageable);
                System.out.println(userComments);
            } else if(lastCommentId != null && isMyComment){
                userComments = commentRepository.findByPostAndUserAndParentIsNullAndIdLessThanAndDeleteYNFalse(post, kakaoUser, lastCommentId, userPageable);
            }

            // 유저가 작성한 댓글이 10개가 안 되면, 부족한 만큼 다른 유저의 댓글 추가
            int remaining = 10 - userComments.getContent().size();
            System.out.println("내 댓글 개수 출력");
            System.out.println(remaining);
            System.out.println(userComments.getSize());
            System.out.println(userComments.getContent().size());
            if (remaining > 0) {
                Pageable remainingPageable = PageRequest.of(0, remaining, otherSort);

                System.out.println("다른 유저 댓글 조회");
                // 2. 다른 유저의 댓글을 조회
                if(lastCommentId == null || isMyComment) {
                    otherComments = commentRepository.findByPostAndUserNotAndParentIsNullAndDeleteYNFalse(post, kakaoUser, remainingPageable);
                }else if(lastCommentId != null && !isMyComment){
                    otherComments = commentRepository.findByPostAndUserNotAndParentIsNullAndIdGreaterThanAndDeleteYNFalse(post, kakaoUser, lastCommentId, remainingPageable);
                }
            }

        } else {
            // 3. 카카오 아이디가 없을 경우, 다른 유저의 댓글만 조회

            if (lastCommentId == null) {
                userComments = commentRepository.findByPostAndParentIsNullAndDeleteYNFalse(post, otherPageable);
            } else {
                userComments = commentRepository.findByPostAndParentIsNullAndIdGreaterThanAndDeleteYNFalse(post, lastCommentId, otherPageable);
            }

        }

        // 3. 유저 댓글과 다른 유저 댓글을 합쳐서 하나의 Slice로 반환
        List<CommentResponse> combinedComments = Stream.concat(
                        userComments.getContent().stream(),
                        otherComments.getContent().stream()
                ).map(this::convertToCommentResponse)
                .collect(Collectors.toList());

        boolean hasNext = otherComments.hasNext() || userComments.hasNext();
        return new SliceImpl<>(combinedComments, userPageable, hasNext);

    }

    public Slice<CommentResponse> getSubComments(Long postId, Long parentId, Long lastCommentId, int size, String kakaoId) {

        // postId에 해당하는 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // parentId에 해당하는 부모 댓글이 존재하는지 확인
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));


        // 정렬 방식: 기본적으로 id의 오름차순
        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, size, sort);

        Slice<Comment> subComments;

        if (lastCommentId == null) {
            subComments = commentRepository.findByPostIdAndParentAndDeleteYNFalse(postId, parentComment, pageable);
        } else {
            subComments = commentRepository.findByPostIdAndParentAndIdGreaterThanAndDeleteYNFalse(postId, parentComment, lastCommentId, pageable);
        }

        return subComments.map(comment -> convertToCommentResponse(comment));
    }



    public Long commentCount(Post post){
        Long commentCount = commentRepository.countByPostAndDeleteYNFalse(post);
        return commentCount;
    }

    // 댓글 수정
    public CommentResponse updateComment(CommentUpdateRequest request, String kakaoId) {
        // 댓글을 ID로 조회
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // 작성자와 현재 사용자 비교
        if (!comment.getUser().getKakaoId().equals(kakaoId)) {
            throw new SecurityException("User not authorized to update this comment");
        }

        // 댓글 내용 수정
        comment.setText(request.getText());
        Comment updatedComment = commentRepository.save(comment);

        return convertToCommentResponse(updatedComment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String kakaoId) {
        // 댓글을 ID로 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // 작성자와 현재 사용자 비교
        if (!comment.getUser().getKakaoId().equals(kakaoId)) {
            throw new SecurityException("User not authorized to delete this comment");
        }

        // 댓글의 대댓글들도 조회
        List<Comment> replies = commentRepository.findAllByParentAndDeleteYNFalse(comment);

        // deleteYN 필드 값을 true로 설정
        comment.setDeleteYN(true);
        commentRepository.save(comment);

        for (Comment reply : replies) {
            reply.setDeleteYN(true);
            commentRepository.save(reply);
        }
    }

    // 댓글 작성
    public CommentResponse createComment(CommentRequest request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findByKakaoId(request.getUserId().toString())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment parent = null;
//        int depth = 0;

        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
//            depth = parent.getDepth() + 1;
        }


        Comment comment = new Comment();

        // 대댓글의 대상(없으면 일반 댓글)
        if(request.getTargetId() != null){
            Long targetId = request.getTargetId();
            Optional<User> targetUser = userRepository.findByKakaoId(targetId.toString());
            if(targetUser.isPresent()){
                // 대댓글 대상 저장
                comment.setTargetId(targetUser.get().getId());
            }else{
                throw new IllegalArgumentException("Target ID not found");
            }
        }



        comment.setPost(post);
        comment.setUser(user);
        comment.setText(request.getText());
        comment.setParent(parent);
        comment.setDate(new Date());
        comment.setDeleteYN(false);

        Comment savedComment = commentRepository.save(comment);

        return convertToCommentResponse(savedComment);
    }

    CommentResponse convertToCommentResponse(Comment comment) {
//        List<CommentResponse> childResponses = commentRepository.findByParentAndDeleteYNFalse(comment).stream()
//                .map(this::convertToCommentResponse)
//                .collect(Collectors.toList());

        long childCount = commentRepository.countByParentAndDeleteYNFalse(comment);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        User user = comment.getUser();
        Author author = null;

        if(user != null){
            author = Author.builder()
                    .kakaoId(user.getKakaoId())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .build();
        }

        // 대댓글의 대상 유저(targetId) 조회
        Author targetUser = null;
        if (comment.getTargetId() != null) {
            User target = userRepository.findById(comment.getTargetId())
                    .orElse(null);
            if (target != null) {
                targetUser = Author.builder()
                        .kakaoId(target.getKakaoId())
                        .name(target.getName())
                        .profileImage(target.getProfileImage())
                        .build();
            }
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(author)
                .date(comment.getDate().toString())
                .date(format.format(comment.getDate()))
                .childrenCount(childCount)
                .accept(comment.isAccept())
                .targetUser(targetUser)
                .build();
    }


    // 댓글 채택
    @Transactional
    public CommentResponse acceptComment(Long postId, Long commentId, String kakaoId) {
        // 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 게시글 작성자인지 확인
        if (!post.getAuthor().getKakaoId().equals(kakaoId)) {
            throw new IllegalArgumentException("Only the post author can accept a comment");
        }

        // 댓글이 존재하는지 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        Comment updateComment = null;

        if (comment.isAccept()) {
            // 이미 채택된 댓글이라면 채택을 취소
            updateComment = comment.toBuilder().accept(false).build();
        } else {
            // 해당 게시글에 이미 채택된 댓글이 있는지 확인
            Optional<Comment> existingAcceptedComment = commentRepository.findByPostAndAcceptTrueAndDeleteYNFalse(post);
            if (existingAcceptedComment.isPresent()) {
                throw new IllegalArgumentException("A comment has already been accepted for this post");
            }

            // 채택되지 않은 댓글이라면 채택
            updateComment = comment.toBuilder().accept(true).build();
            System.out.println(updateComment.isAccept());
        }
        commentRepository.save(updateComment);
        return convertToCommentResponse(updateComment);
    }

    // 채택 댓글 반환
    public CommentResponse getAcceptComment(Long postId) {
        // 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        // 해당 게시글에 채택된 댓글이 있는지 확인
        Comment acceptedComment = commentRepository.findByPostAndAcceptTrueAndDeleteYNFalse(post)
                .orElseThrow(() -> new IllegalArgumentException("No accepted comment for this post"));

        // 채택된 댓글을 반환
        return convertToCommentResponse(acceptedComment);
    }
}
