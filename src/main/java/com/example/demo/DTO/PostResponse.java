package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "게시글 응답")
public class PostResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 작성자 정보")
    private Author author;

    @Schema(description = "게시글 제목", example = "게시글 제목 예시")
    private String title;

    @Schema(description = "게시글 내용", example = "게시글 내용 예시")
    private String content;

    @Schema(description = "게시글 카테고리", example = "질문하기")
    private String category;

    @Schema(description = "게시 날짜", example = "2024-09-13 00:00:00")
    private String date;

    @Schema(description = "좋아요 수", example = "10")
    private long likeCount;

    @Schema(description = "좋아요 여부", example = "true")
    private boolean likeYN;

    @Schema(description = "댓글 수", example = "5")
    private long commentCount;

    @Schema(description = "해시태그 목록", example = "[\"#Java\", \"#SpringBoot\"]")
    private List<String> hashtags;

    @Schema(description = "게시글 사진 URL 목록", example = "[\"http://example.com/photo1.jpg\", \"http://example.com/photo2.jpg\"]")
    private List<String> photoUrls;
}