package com.couple.gallery.couple_gallery_backend.dto;

import jakarta.validation.constraints.Email; // 이메일 형식 검사
import jakarta.validation.constraints.NotBlank; // 공백 금지
import jakarta.validation.constraints.Size; // 길이 제한
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank @Size(min = 4, message = "연결 코드는 4자 이상이어야 합니다.")
    private String connectionCode;
}