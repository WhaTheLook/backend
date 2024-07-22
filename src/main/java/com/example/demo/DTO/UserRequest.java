package com.example.demo.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {
    private String kakaoId;
    private String name;
    private String email;
    private String profileImage;
}