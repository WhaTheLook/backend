package com.example.demo.DTO;

import lombok.Data;

@Data
public class LikeRequest {
    private Long postId;
    private Long userId;
}
