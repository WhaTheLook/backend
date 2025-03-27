package com.example.demo.DTO;

import lombok.Getter;

@Getter
public class CommentRequest {
    private Long postId;
    private Long userId;
    private Long parentId;
    private String text;
    private Long targetId;
}
