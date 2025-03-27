package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String kakaoId;
    private String email;
    private String name;
    private String profileImage;
    private Date date;
    private Long postCount;
    private Long commentCount;
}
