package com.example.demo.Controller;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.UserRequest;
import com.example.demo.DTO.UserResponse;
import com.example.demo.Service.KakaoService;
import com.example.demo.Service.UserService;
import com.example.demo.config.JWTUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name="유저 컨트롤러", description = "회원 정보 전용 컨트롤러")
public class UserController {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @Operation(summary = "로그인", description = "카카오 인가코드를 넘기면 access, refresh 토큰을 반환합니다.")
    @Parameter(name = "code", description = "카카오 인가코드")
    @PostMapping("/login")
    public  ResponseEntity<Map<String, String>> kakaoLogin(@RequestBody LoginRequest login) {

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
    }


    @Operation(summary = "토큰 로그인", description = "카카오 엑세스 토큰을 넘기면 access, refresh 토큰을 반환합니다.")
    @Parameter(name = "accessToken", description = "카카오 토큰")
    @PostMapping("/token/login")
    public  ResponseEntity<Map<String, String>> kakaoTokenLogin(@RequestBody LoginRequest login) {

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
    }



    @Operation(summary = "엑세스 토큰 재발급", description = "리프레시 토큰을 넘기면 엑세스 토큰을 반환합니다.")
    @Parameter(name = "refreshToken", description = "리프레시 토큰")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody String refreshToken) {

        if (jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(400).body(Map.of("error", "Refresh token is expired"));
        }

        Claims claims = jwtUtil.extractAllClaims(refreshToken);
        String kakaoId = claims.getSubject();

        String newAccessToken = jwtUtil.generateAccessToken(kakaoId);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @Operation(summary = "회원 정보", description = "회원정보를 반환합니다")
    @GetMapping("/info")
    public ResponseEntity<UserResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7); // "Bearer " 이후의 토큰만 추출
        Claims claims = jwtUtil.extractAllClaims(jwtToken);

        if (!jwtUtil.isTokenExpired(jwtToken)) {
            String kakaoId = claims.getSubject();
            UserResponse user = userService.getUser(kakaoId);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @Operation(summary = "회원 정보 변경", description = "회원정보를 변경합니다.")
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody UserRequest userRequest) {
        userService.updateUser(userRequest);
        return ResponseEntity.ok("User updated successfully");
    }


    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴")
    @Parameter(name = "kakaoId", description = "카카오에서 받았던 회원 아이디")
    @DeleteMapping("/delete/{kakaoId}")
    public ResponseEntity<String> deleteUser(@RequestParam String kakaoId) {
        String message = userService.deleteUser(kakaoId);
        return ResponseEntity.ok(message);
    }

    }