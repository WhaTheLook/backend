package com.example.demo.Repository;

import com.example.demo.Entity.Like;
import com.example.demo.Entity.Post;
import com.example.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    long countByPost(Post post);

    Like findByPostAndUser(Post post, User user);

    boolean existsByPostAndUserKakaoId(Post post, String kakaoId);
    boolean existsByPostAndId(Post post, Long userId);

}
