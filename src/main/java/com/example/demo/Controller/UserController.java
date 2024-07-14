package com.example.demo.Controller;

import com.example.demo.Service.KakaoService;
import com.example.demo.Service.UserService;
import com.example.demo.config.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @GetMapping("/")
    public  ResponseEntity<Map<String, String>> kakaoLogin(@RequestParam String code) {
        System.out.println("code : ");
        System.out.println(code);
        String accessToken = kakaoService.getKakaoAccessToken(code);
        System.out.println("accessToken : ");
        System.out.println(accessToken);
        Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
        System.out.println("userInfo : ");
        System.out.println(userInfo);
        // 사용자 정보 저장 또는 갱신
        userService.saveUserIfNotExists(userInfo);

        String kakaoId = String.valueOf(userInfo.get("id")); // Kakao 사용자 ID를 username으로 사용
        System.out.println("kakaoId : ");
        System.out.println(kakaoId);
        // JWT 토큰 생성
        String jwtAccessToken = jwtUtil.generateAccessToken(kakaoId);
        String jwtRefreshToken = jwtUtil.generateRefreshToken(kakaoId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtAccessToken);
        tokens.put("refreshToken", jwtRefreshToken);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        String refreshToken = tokenRequest.get("refreshToken");

        if (jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(403).body(Map.of("error", "Refresh token is expired"));
        }

        Claims claims = jwtUtil.extractAllClaims(refreshToken);
        String username = claims.getSubject();

        String newAccessToken = jwtUtil.generateAccessToken(username);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
    }