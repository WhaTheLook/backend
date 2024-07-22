package com.example.demo.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class PostResponse {

    private Long id;
    private String author;
    private String title;
    private String content;
    private String category;
    private String date;
    private long likeCount;
    private List<String> hashtags;
    private List<String> photoUrls;
}