package com.example.demo.DTO;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UserRequest {
    private String kakaoId;
    private String name;
    private String email;
    private String profileImage;
}