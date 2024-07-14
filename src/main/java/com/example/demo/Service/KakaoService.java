package com.example.demo.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String your_kakao_client_id;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String your_redirect_uri;



    public String getKakaoAccessToken(String code) {
        String accessToken = "";
        String refreshToken = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", your_kakao_client_id);
        params.add("redirect_uri", your_redirect_uri);
        params.add("code", code);

        System.out.println("your_kakao_client_id : ");
        System.out.println(your_kakao_client_id);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        System.out.println("request : ");
        System.out.println(request);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<Map> response = restTemplate.postForEntity(reqURL, request, Map.class);

        if(response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            accessToken = (String) responseBody.get("access_token");
            refreshToken = (String) responseBody.get("refresh_token");
        }

        return accessToken;
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        String reqURL = "https://kapi.kakao.com/v2/user/me";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(reqURL, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}