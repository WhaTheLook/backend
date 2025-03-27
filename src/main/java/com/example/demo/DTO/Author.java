package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보")
public class Author {
    @Schema(description = "카카오 아이디", example = "123")
    private String kakaoId;
    @Schema(description = "이름", example = "홍길동")
    private String name;
    @Schema(description = "프로필 이미지 주소", example = "http://example.com/photo1.jpg")
    private String profileImage;
}
