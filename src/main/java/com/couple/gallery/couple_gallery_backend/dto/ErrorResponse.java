package com.couple.gallery.couple_gallery_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ErrorResponse {
    private final String errorCode; // DUPLICATE_EMAIL, INVALID_INPUT 등 구분자
    private final String message;   // 사용자에게 보여줄 상세 메시지
}