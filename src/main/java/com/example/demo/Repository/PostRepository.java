package com.example.demo.Repository;

import com.example.demo.Entity.Post;
import com.example.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndDeleteYNFalse(Long id);
    Slice<Post> findByDeleteYNFalseAndCategory(Pageable pageable, String category);
    Slice<Post> findByDeleteYNFalseAndAuthor(Pageable pageable, User author);
    Slice<Post> findByDeleteYNFalseAndCategoryAndIdLessThan(String category, Long lastPostId, Pageable pageable);
    Slice<Post> findByDeleteYNFalseAndAuthorAndIdLessThan(User author, Long lastPostId, Pageable pageable);
    // 유저 게시판 개수
    long countByAuthorAndDeleteYNFalse(User user);

    // 검색 개수
    @Query("SELECT COUNT(p) FROM Post p JOIN p.hashtags h WHERE p.deleteYN = false AND h.name LIKE %:hashtag%")
    long countByDeleteYNFalseAndHashtagsContaining(String hashtag);


    @Query("SELECT p FROM Post p JOIN Like l ON p.id = l.post.id WHERE l.user = :user AND (:lastPostId IS NULL OR p.id < :lastPostId) AND p.deleteYN = false")
    Slice<Post> findByUserLikes(@Param("user") User user, @Param("lastPostId") Long lastPostId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE h.name LIKE %:name% AND (:lastPostId IS NULL OR p.id < :lastPostId)  AND p.deleteYN = false")
    Slice<Post> findByHashtagName(@Param("name") String name, @Param("lastPostId") Long lastPostId, Pageable pageable);

}
