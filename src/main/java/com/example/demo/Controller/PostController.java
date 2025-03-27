package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.Entity.Post;
import com.example.demo.Service.CommentService;
import com.example.demo.Service.LikeService;
import com.example.demo.Service.PostService;
import com.example.demo.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(name="게시판 컨트롤러", description = "게시판 컨트롤러")
public class PostController {
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final UserService userService;

    @Operation(summary = "게시판 생성", description = "게시판 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 게시글이 생성되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<?> createPost(@RequestPart("postRequest") PostRequest postRequest,
                                        @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {

        try {
            if (photos.size() != 0) {
                postRequest.setPhotos(photos);
            }
            postService.savePost(postRequest);
            return ResponseEntity.ok("Post created successfully");
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "게시판 목록", description = "게시판 목록을 반환합니다.")
    @Parameter(name = "Authorization", description = "헤더에서 받을 토큰")
    @Parameter(name = "lastPostId", description = "마지막에 받은 게시판 아이디 (해당 번호 이후 부터 요청)")
    @Parameter(name = "size", description = "한번에 받을 게시판 개수 (기본값 10)")
    @Parameter(name = "category", description = "검색될 게시판 카테고리")
    @Parameter(name = "sortBy", description = "게시판 정렬순 (기본값 최신순)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 게시물 목록을 반환합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SlicePostAPI.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/postList")
    public ResponseEntity<?> getPosts(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String category,
            @RequestParam(defaultValue = "recent") String sortBy) {

        try {
            return ResponseEntity.ok(postService.getPostList(size, category, sortBy, lastPostId, userService.getKakaoId(token)));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "게시판 검색 목록", description = "게시판 검색 목록을 반환합니다.")
    @Parameter(name = "Authorization", description = "헤더에서 받을 토큰")
    @Parameter(name = "lastPostId", description = "마지막에 받은 게시판 아이디 (해당 번호 이후 부터 요청)")
    @Parameter(name = "size", description = "한번에 받을 게시판 개수 (기본값 10)")
    @Parameter(name = "category", description = "검색될 게시판 카테고리")
    @Parameter(name = "sortBy", description = "게시판 정렬순 (기본값 최신순)")
    @Parameter(name = "search", description = "검색어")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 검색된 게시물 목록을 반환합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/postList/{search}")
    public ResponseEntity<?> getPostsByHashtag(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sortBy,
            @PathVariable String search) {

        try {
            return ResponseEntity.ok(postService.getPostsByHashtag(size, sortBy, lastPostId, search, userService.getKakaoId(token)));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }


    @Operation(summary = "게시판 상세 정보", description = "게시판 상세 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 게시물 상세 정보가 반환됩니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            PostDetailResponse postDetail = postService.getPost(postId, userService.getKakaoId(token));
            return ResponseEntity.ok(postDetail);
        }catch (ResponseStatusException e) {
            // 예외가 ResponseStatusException일 경우 상태 코드를 반환
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "게시판 업데이트", description = "게시판 업데이트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 게시글이 수정됩니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PutMapping("/update")
    public ResponseEntity<?> updatePost(@RequestPart("postUpdateRequest") PostUpdateRequest updateRequest,
                                        @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        updateRequest.setPhotos(photos);
        Post updatedPost = postService.updatePost(updateRequest);
        PostResponse postResponse = postService.convertToPostResponse(updatedPost, updateRequest.getAuthor());
        try{
        return ResponseEntity.ok(postResponse);
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "게시판 삭제", description = "게시판을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 게시글이 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 삭제 실패.")
    })
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
     @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            postService.deletePost(postId, userService.getKakaoId(token));
            return ResponseEntity.ok("Success");
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "좋아요 생성, 삭제", description = "좋아요 존재시 삭제 미존재시 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 좋아요가 수정됩니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LikeResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(@RequestBody LikeRequest likeRequest) {
        try{
        LikeResponse likeResponse = likeService.toggleLike(likeRequest);
        return ResponseEntity.ok(likeResponse);
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "댓글 작성", description = "게시판 댓글을 작성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글이 생성되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PostMapping("/comment/create")
    public ResponseEntity<?> createComment(@RequestBody CommentRequest commentRequest) {
        try{

//        commentService.addComment(commentRequest);
        CommentResponse commentResponse = commentService.createComment(commentRequest);

        return ResponseEntity.ok(commentResponse);
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    // 게시판 댓글 목록
    @Operation(summary = "댓글 목록", description = "게시판 댓글 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{postId}/comment")
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long lastCommentId,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try{
            Slice<CommentResponse> comments = commentService.getComments(postId, lastCommentId, userService.getKakaoId(token));
            return ResponseEntity.ok(comments);
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    // 게시판 대댓글 목록
    @Operation(summary = "대댓글 목록", description = "댓글의 대댓글 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 대댓글목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{postId}/{parentId}/comment")
    public ResponseEntity<?> getSubComments(
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @RequestParam(required = false) Long lastCommentId,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try{
            Slice<CommentResponse> comments = commentService.getSubComments(postId, parentId, lastCommentId, size, userService.getKakaoId(token));
            return ResponseEntity.ok(comments);
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }



    @Operation(summary = "댓글 수정", description = "게시판 댓글 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글이 수정됩니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PutMapping("/{commentId}/update")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request,
            @RequestHeader(name = "Authorization") String token) {

        try{
        // 토큰에서 사용자 ID를 추출하는 로직 (JWT에서 kakaoId 추출)
        String kakaoId = userService.getKakaoId(token);

        CommentResponse updatedComment = commentService.updateComment(request, kakaoId);
        return ResponseEntity.ok("Success");
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "댓글 삭제", description = "게시판 댓글 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글이 삭제 됩니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @DeleteMapping("/{commentId}/delete")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader(name = "Authorization") String token) {

        try{

        // 토큰에서 사용자 ID를 추출하는 로직 (JWT에서 kakaoId 추출)
        String kakaoId = userService.getKakaoId(token);

        commentService.deleteComment(commentId, kakaoId);
        return ResponseEntity.ok("Success");
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    //댓글 채택
    @Operation(summary = "댓글 채택", description = "게시판 댓글 채택")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글이 채택되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PostMapping("/{postId}/{commentId}/accept")
    public ResponseEntity<?> acceptComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader(value = "Authorization") String token) {
        try {
            String kakaoId = userService.getKakaoId(token);
            CommentResponse commentResponse = commentService.acceptComment(postId, commentId, kakaoId);
            return ResponseEntity.ok(commentResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //댓글 채택 확인
    @Operation(summary = "댓글 채택 확인", description = "게시판 댓글 채택 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 채택 댓글이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{postId}/accept")
    public ResponseEntity<?> getAcceptComment(
            @PathVariable Long postId) {
        try {
            CommentResponse commentResponse = commentService.getAcceptComment(postId);
            return ResponseEntity.ok(commentResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

}
