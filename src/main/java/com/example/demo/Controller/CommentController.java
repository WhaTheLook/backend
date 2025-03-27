package com.example.demo.Controller;

import com.example.demo.Service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name="댓글 컨트롤러", description = "댓글 컨트롤러")
public class CommentController {

    private final CommentService commentService;


}
