package com.example.demo.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostRequest {

    private String author;

    private String title;

    private String content;

    private String category;

    private List<String> hashtags;

    private List<MultipartFile> photos;
}
