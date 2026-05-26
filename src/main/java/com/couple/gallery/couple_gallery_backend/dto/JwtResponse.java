package com.couple.gallery.couple_gallery_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    // 토큰 타입 (JWT의 경우 "Bearer" 사용)
    private String grantType = "Bearer";
    // 실제 인증에 사용될 Access Token
    private String accessToken;
    // 토큰 만료 시간
    private Long expiresIn;

    private String username;

    private String email;

    private String profileImageUrl;

    private Long id;

    private Long coupleId;
}