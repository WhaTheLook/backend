package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.Service.KakaoService;
import com.example.demo.Service.PostService;
import com.example.demo.Service.UserService;
import com.example.demo.config.JWTUtil;
import io.jsonwebtoken.Claims;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name="유저 컨트롤러", description = "회원 정보 전용 컨트롤러")
public class UserController {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final PostService postService;
    private final JWTUtil jwtUtil;

    @Operation(summary = "로그인", description = "카카오 인가코드를 넘기면 access, refresh 토큰을 반환합니다.")
    @Parameter(name = "code", description = "카카오 인가코드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 토큰이 반환되었습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "로그인 실패 또는 잘못된 인가 코드입니다.")
    })
    @PostMapping("/login")
    public  ResponseEntity<?> kakaoLogin(@RequestBody LoginRequest login) {
        try {
            String accessToken = kakaoService.getKakaoAccessToken(login.getCode());
            Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
            // 사용자 정보 저장 또는 갱신
            userService.saveUserIfNotExists(userInfo);

            String kakaoId = String.valueOf(userInfo.get("id")); // Kakao 사용자 ID를 username으로 사용
            // JWT 토큰 생성
            String jwtAccessToken = jwtUtil.generateAccessToken(kakaoId);
            String jwtRefreshToken = jwtUtil.generateRefreshToken(kakaoId);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtAccessToken);
            tokens.put("refreshToken", jwtRefreshToken);
            return ResponseEntity.ok(tokens);
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }


    @Operation(summary = "토큰 로그인", description = "카카오 엑세스 토큰을 넘기면 access, refresh 토큰을 반환합니다.")
    @Parameter(name = "accessToken", description = "카카오 토큰")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 토큰이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "로그인 실패 또는 잘못된 인가 코드입니다.")
    })
    @PostMapping("/token/login")
    public  ResponseEntity<?> kakaoTokenLogin(@RequestBody LoginRequest login) {

        try{

        Map<String, Object> userInfo = kakaoService.getUserInfo(login.getCode());
        // 사용자 정보 저장 또는 갱신
        userService.saveUserIfNotExists(userInfo);

        String kakaoId = String.valueOf(userInfo.get("id")); // Kakao 사용자 ID를 username으로 사용
        // JWT 토큰 생성
        String jwtAccessToken = jwtUtil.generateAccessToken(kakaoId);
        String jwtRefreshToken = jwtUtil.generateRefreshToken(kakaoId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtAccessToken);
        tokens.put("refreshToken", jwtRefreshToken);

        return ResponseEntity.ok(tokens);

        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }

    }

    @Operation(summary = "테스트 토큰 로그인", description = "카카오 엑세스 토큰을 넘기면 access, refresh 토큰을 반환합니다.")
    @Parameter(name = "accessToken", description = "카카오 토큰")
    @PostMapping("/test/token/login")
    public  ResponseEntity<?> testKakaoTokenLogin(@RequestBody LoginRequest login) {

        try{

            Map<String, Object> userInfo = kakaoService.getUserInfo(login.getCode());
            // 사용자 정보 저장 또는 갱신
            userService.saveUserIfNotExists(userInfo);

            String kakaoId = String.valueOf(userInfo.get("id")); // Kakao 사용자 ID를 username으로 사용
            // JWT 토큰 생성
            String jwtAccessToken = jwtUtil.testGenerateAccessToken(kakaoId);
            String jwtRefreshToken = jwtUtil.testGenerateRefreshToken(kakaoId);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtAccessToken);
            tokens.put("refreshToken", jwtRefreshToken);

            return ResponseEntity.ok(tokens);

        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }

    }

    @Operation(summary = "엑세스 토큰 만료 확인", description = "엑세스 토큰의 만료 확인")
    @Parameter(name = "accessToken", description = "엑세스 토큰 (헤더 토큰)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 엑세스 토큰 만료 확인 결과 반환"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PostMapping("/token/check")
    public ResponseEntity<?> accessToken(@RequestHeader(value = "Authorization") String accessToken) {
        System.out.println(accessToken);
        try {
            String jwtToken = accessToken.substring(7); // "Bearer " 이후의 토큰만 추출
            if (jwtUtil.isTokenExpired(jwtToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "token is expired"));
            } else {
                return ResponseEntity.status(200).body(Map.of("success", "token is not expired"));
            }
        }catch (Exception e){
            System.out.println(e.toString());
        return ResponseEntity.status(400).body(e.toString());
        }

    }



    @Operation(summary = "엑세스 토큰 재발급", description = "리프레시 토큰을 넘기면 엑세스 토큰을 반환합니다.")
    @Parameter(name = "refreshToken", description = "리프레시 토큰")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 토큰이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "로그인 실패 또는 잘못된 인가 코드입니다.")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestHeader(value = "Authorization") String refreshToken) {
        try {
            String newAccessToken = null;
            String newRefreshToken = null;
            String jwtToken = refreshToken.substring(7); // "Bearer " 이후의 토큰만 추출
            if (jwtUtil.isTokenExpired(jwtToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Refresh token is expired"));
            }

            Claims claims = jwtUtil.extractAllClaims(jwtToken);
            String kakaoId = claims.getSubject();

            newAccessToken = jwtUtil.generateAccessToken(kakaoId);
            newRefreshToken = jwtUtil.generateRefreshToken(kakaoId);
            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
            ));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @GetMapping("/testrefresh")
    public ResponseEntity<?> testRefreshToken(@RequestParam(defaultValue = "3612056684") String kakaoId) {
        try {
            String newAccessToken = null;
            String newRefreshToken = null;

            newAccessToken = jwtUtil.generateAccessToken(kakaoId);
            newRefreshToken = jwtUtil.generateRefreshToken(kakaoId);
            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
            ));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "회원 정보", description = "회원정보를 반환합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원 정보가 반환됩니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        try{
        String jwtToken = token.substring(7); // "Bearer " 이후의 토큰만 추출
        Claims claims = jwtUtil.extractAllClaims(jwtToken);

        if (!jwtUtil.isTokenExpired(jwtToken)) {
            String kakaoId = claims.getSubject();
            UserResponse user = userService.getUser(kakaoId);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(400).build();
        }
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "회원의 게시판", description = "회원이 작성한 게시물 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원 게시판 목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SlicePostAPI.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{kakaoId}/post")
    public ResponseEntity<?> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sortBy
    ) {
        try {
            return ResponseEntity.ok(postService.userPostList(size, sortBy, lastPostId, userService.getKakaoId(token)));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "회원의 북마크 게시판", description = "회원이 북마크한 게시물 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원 북마크 목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SlicePostAPI.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{kakaoId}/likePost")
    public ResponseEntity<?> getUserLikePost(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sortBy
    ) {
        try {
            return ResponseEntity.ok(postService.userLikePostList(size, sortBy, lastPostId, userService.getKakaoId(token)));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "회원의 댓글 게시판", description = "회원이 작성한 댓글 게시물 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원 댓글목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SlicePostAPI.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/{kakaoId}/commentPost")
    public ResponseEntity<?> getUserCommentPost(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sortBy
    ) {
        try {
            String kakaoId = userService.getKakaoId(token);
            return ResponseEntity.ok(postService.getPostsByUserComments(kakaoId, lastPostId, size));
        }catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.status(400).body(e.toString());
        }
    }

    @Operation(summary = "회원 정보 변경", description = "회원정보를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원 정보가 수정되었습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestPart("userRequest") UserRequest userRequest,
                                             @RequestPart(value = "profileImage", required = false)MultipartFile profileImage) {
        try {

        return ResponseEntity.ok(userService.updateUser(userRequest, profileImage));
        }catch (Exception e){
            System.out.println(e.toString());
        return ResponseEntity.status(400).body(e.toString());
        }
    }


    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴")
    @Parameter(name = "kakaoId", description = "카카오에서 받았던 회원 아이디")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원 탈퇴되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @DeleteMapping("/delete/{kakaoId}")
    public ResponseEntity<?> deleteUser(@RequestParam String kakaoId) {
        String message = null;
        try {
            message = userService.deleteUser(kakaoId);
        return ResponseEntity.ok(message);
        }catch (Exception e){
            System.out.println(e.toString());
        return ResponseEntity.status(400).body(e.toString());
    }
    }

    }