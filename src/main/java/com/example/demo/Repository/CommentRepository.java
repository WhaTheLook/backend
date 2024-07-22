package com.example.demo.Repository;

import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostAndDeleteYNOrderByOrderAsc(Post post, Boolean deleteYN);

    long countByPost(Post post);

    List<Comment> findByPostAndOrderGreaterThanEqual(Post post, int order);
}
