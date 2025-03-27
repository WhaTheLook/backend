package com.example.demo.DTO;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class PostUpdateRequest {

    private Long id;  // 게시물 ID

    private String author;       // 작성자
    private String title;        // 제목
    private String content;      // 내용
    private String category;     // 카테고리
    private List<String> hashtags;  // 해시태그 목록
    private List<MultipartFile> photos;  // 사진 목록

}