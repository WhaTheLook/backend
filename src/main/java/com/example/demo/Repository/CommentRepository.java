package com.example.demo.Repository;

import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Post;
import com.example.demo.Entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
//    List<Comment> findAllByPostAndDeleteYNOrderByOrderAsc(Post post, Boolean deleteYN);
    long countByPost(Post post);
//    List<Comment> findByPostAndOrderGreaterThanEqual(Post post, int order);
    // 유저 댓글 개수
    long countByUserAndDeleteYNFalse(User user);

    long countByPostAndDeleteYNFalse(Post post);

    List<Comment> findAllByParentAndDeleteYNFalse(Comment parent);



    Slice<Comment> findByPostAndParentIsNullAndDeleteYNFalse(Post post, Pageable pageable);
    List<Comment> findByParentAndDeleteYNFalse(Comment parent);
    // 대댓글 개수
    long countByParentAndDeleteYNFalse(Comment parent);

    // lastCommentId 이후의 댓글을 가져오기 위한 쿼리
    Slice<Comment> findByPostAndParentIsNullAndIdGreaterThanAndDeleteYNFalse(Post post, Long lastCommentId, Pageable pageable);


    // 사용자의 댓글 목록
    Slice<Comment> findByPostAndUserAndParentIsNullAndDeleteYNFalse(Post post, User user, Pageable pageable);
    Slice<Comment> findByPostAndUserAndParentIsNullAndIdLessThanAndDeleteYNFalse(Post post, User user, Long lastCommentId, Pageable pageable);

    // 사용자 제외 다른 유저 댓글 목록
    Slice<Comment> findByPostAndUserNotAndParentIsNullAndDeleteYNFalse(Post post, User user, Pageable pageable);
    Slice<Comment> findByPostAndUserNotAndParentIsNullAndIdGreaterThanAndDeleteYNFalse(Post post, User user, Long lastCommentId,  Pageable pageable);

    //대댓글 목록
    Slice<Comment> findByPostIdAndParentAndDeleteYNFalse(Long postId, Comment parent, Pageable pageable);
    Slice<Comment> findByPostIdAndParentAndIdGreaterThanAndDeleteYNFalse(Long postId, Comment parent, Long lastCommentId, Pageable pageable);


    Optional<Comment> findByPostAndAcceptTrueAndDeleteYNFalse(Post post);


    // 특정 회원이 작성한 댓글을 통해 게시글을 중복 없이 조회, 페이징 처리 및 lastPostId를 이용
    @Query("SELECT DISTINCT c.post FROM Comment c WHERE c.user.kakaoId = :kakaoId AND (:lastPostId IS NULL OR c.post.id < :lastPostId)AND c.deleteYN = false")
    Slice<Post> findDistinctPostsByAuthorKakaoIdAndPostIdLessThanAndDeleteYNFalse(
            @Param("kakaoId") String kakaoId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable
    );

    List<Comment> findByPost(Post post);
}
