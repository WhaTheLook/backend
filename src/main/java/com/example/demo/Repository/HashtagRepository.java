package com.example.demo.Repository;

import com.example.demo.Entity.Hashtag;
import com.example.demo.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    void deleteByPost(Post post);
}
