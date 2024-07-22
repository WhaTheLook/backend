package com.example.demo.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {

    private Long id;          // 댓글 ID
    private String author;   // 댓글 작성자
    private String content;  // 댓글 내용
    private String date;     // 댓글 작성 시간 (상대적 시간)
    private int depth;       // 댓글 깊이 (부모 댓글 대비 깊이)
    private int order;       // 댓글 순서
}
