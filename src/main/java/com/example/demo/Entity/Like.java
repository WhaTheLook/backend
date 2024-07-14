package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Like를 한 사용자 정보 추가 (예: User 엔티티와의 관계)
     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;
}