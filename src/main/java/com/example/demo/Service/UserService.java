package com.example.demo.Service;

import com.example.demo.DTO.Author;
import com.example.demo.DTO.UserRequest;
import com.example.demo.DTO.UserResponse;
import com.example.demo.Entity.User;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.config.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final JWTUtil jwtUtil;

    public User saveUserIfNotExists(Map<String, Object> userInfo) {
        System.out.println(userInfo);
        String kakaoId = String.valueOf(userInfo.get("id"));
        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String name = (String) ((Map<String, Object>) userInfo.get("properties")).get("nickname");
        String profile_image = (String) ((Map<String, Object>) userInfo.get("properties")).get("profile_image");
        System.out.println((String) ((Map<String, Object>) userInfo.get("properties")).get("profile_image"));
        return userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User newUser = new User();
            newUser.setKakaoId(kakaoId);
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setProfileImage(profile_image);
            return userRepository.save(newUser);
        });
    }

    public UserResponse getUser(String kakaoId) {
        // 카카오 ID를 이용해 사용자 정보를 로드
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with kakaoId: " + kakaoId));

        // 삭제 되지 않은 게시물 개수
        Long postCount = postRepository.countByAuthorAndDeleteYNFalse(user);
        Long commentCount = commentRepository.countByUserAndDeleteYNFalse(user);

        // 로드된 사용자 정보를 UserResponse 객체로 변환
        return  new UserResponse().builder()
                .kakaoId(user.getKakaoId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .date(user.getDate())
                .postCount(postCount)
                .commentCount(commentCount)
                .build();
    }

    public Author updateUser(UserRequest userRequest, MultipartFile profileImage) throws IOException {
        Optional<User> optionalUser = userRepository.findByKakaoId(userRequest.getKakaoId());
        System.out.println(userRequest.getKakaoId());
        System.out.println("유저 카카오 아이디");
        User user = optionalUser.get();
        String url = null;

        System.out.println("유저 정보는 찾음");

        // 유저가 존재 한다면
        if (optionalUser.isPresent()) {
            System.out.println("여기도 나옴");
            // 사진 변경
            if(profileImage != null){
                url = s3Service.uploadFile(profileImage);
                user.setProfileImage(url);
            }
            // 유저 이름 변경
            if(userRequest.getName() != null){
                user.setName(userRequest.getName());
            }
            userRepository.save(user);
            return new Author().builder()
                    .kakaoId(user.getKakaoId())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .build();

        } else {
            throw new RuntimeException("User not found");
        }
    }

    public String deleteUser(String kakaoId) {
        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);

        if (optionalUser.isPresent()) {
            userRepository.delete(optionalUser.get());
            return "User deleted successfully";
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public String getKakaoId(String token){
        if(token != null){
            String jwtToken = token.substring(7);
            Claims claims = jwtUtil.extractAllClaims(jwtToken);
            String kakaoId = claims.getSubject();
            return kakaoId;
        }else{
            return null;
        }
    }
}
