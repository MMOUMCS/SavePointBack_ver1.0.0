package com.couple.gallery.couple_gallery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoupleConnectRequest {

    @NotBlank(message = "상대방 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String targetEmail;

    @NotBlank(message = "커플 연결 코드는 필수입니다.")
    private String connectionCode;
}