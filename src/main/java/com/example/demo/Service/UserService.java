package com.example.demo.Service;

import com.example.demo.DTO.UserRequest;
import com.example.demo.DTO.UserResponse;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User saveUserIfNotExists(Map<String, Object> userInfo) {
        String kakaoId = String.valueOf(userInfo.get("id"));
        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String name = (String) ((Map<String, Object>) userInfo.get("properties")).get("nickname");
        String profile_image = (String) ((Map<String, Object>) userInfo.get("properties")).get("profile_image");

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

        // 로드된 사용자 정보를 UserResponse 객체로 변환
        return  new UserResponse().builder()
                .kakaoId(user.getKakaoId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .date(user.getDate())
                .build();
    }

    public void updateUser(UserRequest userRequest) {
        Optional<User> optionalUser = userRepository.findByKakaoId(userRequest.getKakaoId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEmail(userRequest.getEmail());
            user.setName(userRequest.getName());
            user.setProfileImage(userRequest.getProfileImage());
            userRepository.save(user);

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
}
