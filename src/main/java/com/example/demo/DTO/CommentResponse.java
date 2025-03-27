package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "댓글 응답")
public class CommentResponse {
    @Schema(description = "댓글 ID", example = "1")
    private Long id;          // 댓글 ID
    @Schema(description = "댓글 작성자 정보")
    private Author author;   // 댓글 작성자
    @Schema(description = "댓글 내용", example = "이것은 댓글입니다.")
    private String text;  // 댓글 내용
    @Schema(description = "댓글 작성 시간", example = "2024-09-13 00:00:00")
    private String date;     // 댓글 작성 시간
    @Schema(description = "댓글 채택 여부", example = "false")
    private boolean accept;
    @Schema(description = "대댓글 개수")
    private long childrenCount; // 대댓글 개수
    @Schema(description = "타겟 사용자의 정보 (대댓글에서 대상을 나타냄)")
    private Author targetUser;
}
