package com.couple.gallery.couple_gallery_backend.controller;


// 프론트엔드에서 온 요청을 가장 먼저 받아서 서비스 부서에 전달하고 답장을 보내는 안내 데스크.

import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.dto.*;
import com.couple.gallery.couple_gallery_backend.service.UserService;
import com.couple.gallery.couple_gallery_backend.endpoint.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // @Valid 어노테이션 사용을 위해 필요
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "유저 인증 및 회원 관리 API", description = "회원가입, JWT 로그인, 이메일별 유저 조회 및 프로필 이미지 수정 기능을 제공합니다.")
@RestController // 프론트엔드와 데이터를 주고받기 위함.
@RequestMapping("/api/users") // 데스크 주소 지정.
public class UserController {

    private final UserService userService; // 의존성 주입. 내 일 뒷바라지 해 줄 객체 하나만 연결해줘.

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==========================================
    // 1. 회원가입 API (JSON만 받음)
    // ==========================================
    @Operation(
            summary = "회원가입",
            description = "새로운 유저 정보를 등록합니다. 비밀번호는 서버에서 자동으로 암호화되어 저장됩니다."
    )
    @PostMapping(endpoint.USER_REGISTER)
    public ResponseEntity<User> register(@RequestBody @Valid UserRegisterRequest request) {
        // @RequestBody: json을 객체(class)로 변환해서 전달.
        // @Valid: 전달 전에 빈 칸이 있거나 형식에 안맞으면 입구컷.
        // UserRegisterRequest 라는 DTO.
        // ㄴ DTO : 데이터 임시 저장소 (데이터가 들어오고 나면 쓰레기통에 버림) (프론트->백엔드)
        // ㄴ ENTITY : 실제 저장되는 핵심 데이터 객체. DB와 1:1 매핑(연결). (백엔드 내에서 사용하기 좋게 조립)
        // ㄴ DOMAIN : ENTITY의 모임.
        // !! ENTITY와 DTO를 분리하는 이유 : 그대로 노출시키거나 통신도구로 사용하면 보안상 민감한 정보가 외부에 노출될 위험이 있고,
        // 데이터 포맷이 바뀔때마다 DB까지 수정해야하는 문제 발생.

        // 1) 프론트가 보낸 상자를 열어서 도메인(User) 객체로 조립.
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .connectionCode(request.getConnectionCode())
                .build();

        // 2) 실제 저장을 담당하는 UserService에 배달.
        User savedUser = userService.registerNewUser(user);

        // 3) 성공했다는 편지(HttpStatus.CREATED)와 함께 저장된 정보를 프론트에게 전달.
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // ==========================================
    // 2. 프로필 이미지 수정 API (회원가입 후 별도 호출)
    // ==========================================
    @Operation(
            summary = "프로필 이미지 수정/등록",
            description = "로그인한 유저의 프로필 사진을 S3 버킷에 업로드하고 변경합니다. (Multipart 형식 요청)"
    )
    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateProfileImage(
            @AuthenticationPrincipal UserDetails userDetails, // 토큰에서 정보 획득
            @RequestPart("file") MultipartFile file) { // @RequestParam 대신 @RequestPart 권장

        // 서비스로 이메일(식별자)과 파일을 넘김
        User updatedUser = userService.updateProfileImage(userDetails.getUsername(), file);

        return ResponseEntity.ok(updatedUser);
    }

    // ==========================================
    // 3. 기타 조회 및 로그인 API
    // ==========================================
    @Operation(
            summary = "이메일로 유저 정보 조회️",
            description = "지정한 이메일 주소(email)를 가진 유저의 상세 정보를 조회합니다."
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "로그인 및 토큰 발급",
            description = "이메일과 비밀번호를 검증하여, 향후 API 요청 시 사용할 JWT 엑세스 토큰을 발급합니다."
    )
    @PostMapping(endpoint.USER_LOGIN)
    public ResponseEntity<JwtResponse> login(@RequestBody UserLoginRequest loginRequest) {
        JwtResponse jwtResponse = userService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }


    // ==========================================
    // 4. 프로필 텍스트 정보 수정 API (이름, 소개)
    // ==========================================
    @Operation(
            summary = "프로필 텍스트 수정",
            description = "로그인한 유저의 이름(username)과 한 줄 소개(bio)를 변경합니다. R2 이미지 업로드를 타지 않습니다."
    )
    @PutMapping("/profile") // 프론트에서 보낸 PUT `${API_BASE_URL}/api/users/profile`을 매핑
    public ResponseEntity<User> updateProfileText(
            @AuthenticationPrincipal UserDetails userDetails, // 토큰에서 유저 정보 획득!
            @RequestBody @Valid ProfileRequest request
    ) {
        // userDetails.getUsername()으로 이메일을 뽑아서 서비스로 넘깁니다.
        User updatedUser = userService.updateProfileText(userDetails.getUsername(), request);

        return ResponseEntity.ok(updatedUser);
    }

    // ==========================================
// 5. 비밀번호 변경 API
// ==========================================
    @Operation(
            summary = "비밀번호 변경",
            description = "현재 비밀번호를 확인 후 새 비밀번호로 변경합니다."
    )
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequest request
    ) {
        try {
            userService.updatePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 현재 비밀번호 불일치 시 400 반환
            return ResponseEntity.badRequest().build();
        }
    }
}
