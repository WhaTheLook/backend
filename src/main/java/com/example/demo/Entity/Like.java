package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "`like`")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Like를 한 사용자 정보 추가 (예: User 엔티티와의 관계)
     @ManyToOne
     @JoinColumn(name = "kakaoId", referencedColumnName = "kakaoId")
     private User user;
}