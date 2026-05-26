package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.config.security.UserDetailsImpl;
import com.couple.gallery.couple_gallery_backend.domain.Couple;
import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.dto.PartnerResponse;
import com.couple.gallery.couple_gallery_backend.endpoint.endpoint;
import com.couple.gallery.couple_gallery_backend.dto.CoupleConnectRequest;
import com.couple.gallery.couple_gallery_backend.repository.CoupleRepository;
import com.couple.gallery.couple_gallery_backend.repository.UserRepository;
import com.couple.gallery.couple_gallery_backend.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "커플 연동 및 파트너 API", description = "상대방과 커플 코드로 연결하고, 파트너의 프로필 정보를 조회하는 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
// 기본 경로 설정: /api/couples
@RequestMapping("/api/v1/couples")
public class CoupleController {

    private final CoupleService coupleService;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    /**
     * POST /api/couples/connect
     * 현재 로그인된 사용자와 요청 본문의 상대방 이메일을 커플로 연결합니다.
     */
    @Operation(
            summary = "커플 연결하기",
            description = "상대방의 이메일과 초대 코드를 사용하여 두 사용자를 하나의 커플로 매칭합니다. 성공 시 커플 데이터가 생성됩니다."
    )
    @PostMapping(endpoint.COUPLE_CONNECT)
    public ResponseEntity<?> connectCouple(@Valid @RequestBody CoupleConnectRequest request) {
        try {
            Couple couple = coupleService.connectCouple(request.getTargetEmail(), request.getConnectionCode());
            return new ResponseEntity<>(couple, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 콘솔에 한 번 더 찍어서 서버에서 메시지가 살아있는지 확인
            System.out.println("에러 발생: " + e.getMessage());

            // 명확하게 JSON 구조를 가진 객체를 리턴
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage())); // 전용 DTO 사용 추천
        }
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    static class ErrorResponse {
        private String message;
    }

    @Transactional(readOnly = true)
    public  Couple findCoupleByUserId(Long userId) {
        // user1Id 또는 user2Id 중 하나가 userId와 일치하는 커플을 찾습니다.
        return coupleRepository.findByUser1IdOrUser2Id(userId, userId)
                .orElse(null); // 커플이 없을 경우 null 반환
    }

    /**
     * GET /api/v1/couples/partner
     * 현재 로그인한 사용자의 상대방(파트너) 정보 조회
     */
    @Operation(
            summary = "내 파트너 정보 조회",
            description = "현재 로그인한 사용자와 연결된 상대방(파트너)의 회원 ID, 닉네임, 프로필 이미지 URL, 이메일 정보를 조회합니다."
    )
    @GetMapping("/partner")
    public ResponseEntity<PartnerResponse> getPartnerInfo() {
        // 1. SecurityContext에서 인증된 토큰의 Principal(UserDetailsImpl)을 꺼냅니다.
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        // 2. 이미 UserDetailsImpl에 내 고유 ID(Long)가 있으므로 안전하게 세션 조회
        User me = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. 서비스를 통해 상대방 User 엔티티 조회
        User partner = coupleService.getPartnerInfo(me.getId());

        // 4. DTO로 변환하여 반환 (User 엔티티의 닉네임 필드인 username을 꺼냅니다)
        PartnerResponse response = PartnerResponse.builder()
                .id(partner.getId())
                .name(partner.getUsername()) //  User 엔티티의 username 필드(=닉네임) 호출!
                .profileImageUrl(partner.getProfileImageUrl())
                .email(partner.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }
}