package com.example.demo.Repository;

import com.example.demo.Entity.Photo;
import com.example.demo.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    void deleteByPostAndUrlIn(Post post, List<String> urls);
    void deleteAllByPost(Post post);
    List<Photo> findByPost(Post post);

}
