package com.example.demo.DTO;


import com.example.demo.Entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String author;
    private String title;
    private String content;
    private String category;
    private String date; // 상대적 시간 표현
    private Boolean deleteYN;
    private long likeCount; // 좋아요 수
    private List<String> hashtags; // 해시태그 목록
    private List<String> photoUrls; // 사진 URL 목록
    private List<CommentResponse> comments; // 댓글 목록
}