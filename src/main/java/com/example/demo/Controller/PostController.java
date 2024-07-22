package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.Entity.Post;
import com.example.demo.Service.CommentService;
import com.example.demo.Service.LikeService;
import com.example.demo.Service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(name="게시판 컨트롤러", description = "게시판 컨트롤러")
public class PostController {
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    @Operation(summary = "게시판 생성", description = "게시판 생성")
    @PostMapping("/create")
    public ResponseEntity<String> createPost(@RequestBody PostRequest postDto) throws IOException {
        postService.savePost(postDto);
        return ResponseEntity.ok("Post created successfully");
    }

    @Operation(summary = "게시판 목록", description = "게시판 목록을 반환합니다.")
    @Parameter(name = "page", description = "페이지 번호 (기본값 0)")
    @Parameter(name = "size", description = "한번에 받을 게시판 개수 (기본값 10)")
    @GetMapping("/postList")
    public Page<PostResponse> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.getPostList(page, size);
    }

    @Operation(summary = "게시판 정보", description = "게시판 상세 정보를 반환합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable Long postId) {
        PostDetailResponse postDetail = postService.getPost(postId);
        return ResponseEntity.ok(postDetail);
    }

    @Operation(summary = "게시판 업데이트", description = "게시판 업데이트")
    @PutMapping("/update")
    public ResponseEntity<Post> updatePost(@RequestBody PostUpdateRequest updateRequest) throws IOException {
        Post updatedPost = postService.updatePost(updateRequest);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "좋아요 생성, 삭제", description = "좋아요 존재시 삭제 미존재시 생성")
    @PostMapping("/like")
    public ResponseEntity<String> toggleLike(@RequestBody LikeRequest likeRequest) {
        likeService.toggleLike(likeRequest);
        return ResponseEntity.ok("Like toggled");
    }

    @Operation(summary = "댓글 작성", description = "게시판 댓글을 작성")
    @PostMapping("/comment/create")
    public ResponseEntity<String> createComment(@RequestBody CommentRequest commentRequest) {
        commentService.addComment(commentRequest);
        return ResponseEntity.ok("Success");
    }

    @Operation(summary = "댓글 변경", description = "게시판 댓글을 변경합니다.")
    @PutMapping("/comment/update")
    public ResponseEntity<String> updateComment(@RequestBody CommentUpdateRequest commentRequest) {
        commentService.updateComment(commentRequest);
        return ResponseEntity.ok("Success");
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/comment/delete/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Success");
    }

}
