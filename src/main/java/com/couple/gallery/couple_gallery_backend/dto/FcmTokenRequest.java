package com.couple.gallery.couple_gallery_backend.dto;
// 토큰 저장할 때 쓰는 데이터 껍데기
import lombok.Data;

@Data
public class FcmTokenRequest {
    private String token;
}