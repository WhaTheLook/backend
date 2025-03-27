package com.example.demo.DTO;


import com.example.demo.Entity.Comment;
import com.example.demo.Entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 상세 응답")
public class PostDetailResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;
    @Schema(description = "작성자 정보")
    private Author author;
    @Schema(description = "게시글 제목", example = "게시글 제목 예시")
    private String title;
    @Schema(description = "게시글 내용", example = "게시글 내용 예시")
    private String content;
    @Schema(description = "게시글 카테고리", example = "질문하기")
    private String category;
    @Schema(description = "게시글 작성 시간", example = "2024-09-13 00:00:00")
    private String date;
    @Schema(description = "게시글 삭제 여부", example = "false")
    private Boolean deleteYN;
    @Schema(description = "좋아요 수", example = "10")
    private long likeCount; // 좋아요 수
    @Schema(description = "댓글 수", example = "5")
    private long commentCount; // 댓글 수
    @Schema(description = "사용자가 좋아요를 눌렀는지 여부", example = "true")
    private boolean likeYN;
    @Schema(description = "해시태그 목록", example = "[\"#Java\", \"#SpringBoot\"]")
    private List<String> hashtags; // 해시태그 목록
    @Schema(description = "사진 URL 목록", example = "[\"http://example.com/photo1.jpg\", \"http://example.com/photo2.jpg\"]")
    private List<String> photoUrls; // 사진 URL 목록
    @Schema(description = "댓글 목록")
    private List<CommentResponse> comments; // 댓글 목록
    @Schema(description = "채택된 댓글")
    private CommentResponse accept; //채택 댓글
}