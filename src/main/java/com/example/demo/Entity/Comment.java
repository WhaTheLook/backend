package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private int depth;
    private int order;
    private Date date;
    private Boolean deleteYN;

    // 대댓글의 경우 부모 댓글
    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Comment를 작성한 사용자 정보 추가 (예: User 엔티티와의 관계)
     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;
}