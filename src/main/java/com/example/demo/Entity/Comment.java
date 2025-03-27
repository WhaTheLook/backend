package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
//    private int depth;
    private Long targetId;
    private boolean accept;
//    private int order;
    private Date date;
    private Boolean deleteYN;

    // 대댓글의 경우 부모 댓글
    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment parent;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Comment를 작성한 사용자 정보 추가 (예: User 엔티티와의 관계)
     @ManyToOne
     @JoinColumn(name = "kakaoId")  // Comment 테이블의 컬럼 명
     // 식제 저장 값은 kakao Id 가 아닌 user ID
     private User user;
}