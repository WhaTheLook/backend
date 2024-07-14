package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User saveUserIfNotExists(Map<String, Object> userInfo) {
        String kakaoId = String.valueOf(userInfo.get("id"));
        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String nickname = (String) ((Map<String, Object>) userInfo.get("properties")).get("nickname");

        return userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User newUser = new User();
            newUser.setKakaoId(kakaoId);
            newUser.setEmail(email);
            newUser.setNickname(nickname);
            return userRepository.save(newUser);
        });
    }
}
