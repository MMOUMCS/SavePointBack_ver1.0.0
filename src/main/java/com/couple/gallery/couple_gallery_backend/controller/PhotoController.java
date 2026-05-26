package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.domain.Couple;
import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.service.CoupleService;
import com.couple.gallery.couple_gallery_backend.service.PhotoService;
import com.couple.gallery.couple_gallery_backend.service.UserService; // UserService import
import com.couple.gallery.couple_gallery_backend.dto.PhotoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 커플 앨범 사진 관련 API를 처리하는 컨트롤러.
 * JWT 토큰을 통해 인증된 사용자의 정보를 추출하여 사진 업로드, 조회, 삭제 기능을 제공합니다.
 */
@Tag(name = "커플 앨범 사진 API", description = "S3를 연동하여 커플 공동 앨범에 사진을 업로드하고, 전체 조회 및 삭제하는 기능을 제공합니다.")
@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor // final 필드들을 주입받기 위한 Lombok 어노테이션
@SecurityRequirement(name = "bearerAuth")
public class PhotoController {

    private final PhotoService photoService;
    private final UserService userService;     // 사용자 정보를 DB에서 조회하기 위한 서비스
    private final CoupleService coupleService; // 커플 정보를 DB에서 조회하기 위한 서비스

    /**
     * 현재 로그인한 사용자의 ID와 커플 ID를 추출하는 헬퍼 메서드.
     * Spring Security의 SecurityContextHolder에서 Authentication 객체를 가져와 처리합니다.
     *
     * @return CurrentUser 객체 (userId와 coupleId 포함, coupleId는 null일 수 있음)
     * @throws IllegalArgumentException 로그인 정보가 없거나 유효하지 않을 경우
     */
    private CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 인증 정보 유효성 검사
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        Object principal = authentication.getPrincipal(); // 인증된 주체 (주로 UserDetails 객체)

        Long currentUserId = null;
        Long currentCoupleId = null;

        // 2. UserDetails에서 사용자 이메일 추출 후 DB에서 User 엔티티 조회
        if (principal instanceof UserDetails) {
            String userEmail = ((UserDetails) principal).getUsername(); // JWT subject에 저장된 사용자 이메일
            User user = userService.findUserByEmail(userEmail); // 이메일로 User 엔티티 조회

            currentUserId = user.getId(); // User 엔티티에서 사용자 ID 추출

            // 3. User ID를 이용해 Couple 테이블에서 커플 정보 조회
            Couple couple = coupleService.findCoupleByUserId(currentUserId);
            if (couple != null) {
                currentCoupleId = couple.getId(); // 커플 정보가 있을 경우 coupleId 추출
            }
        } else {
            // UserDetails 타입이 아닌 다른 Principal이 들어왔을 경우 (예외 처리)
            throw new IllegalArgumentException("지원하지 않는 인증 Principal 타입입니다.");
        }

        // 4. 사용자 ID 필수 검사
        if (currentUserId == null) {
            throw new IllegalArgumentException("사용자 ID를 찾을 수 없습니다.");
        }

        return new CurrentUser(currentUserId, currentCoupleId); // coupleId는 커플 연동 전에는 null일 수 있음
    }

    /**
     * 내부적으로 사용될 DTO 클래스.
     * 현재 로그인한 사용자의 ID와 커플 ID를 캡슐화합니다.
     */
    private static class CurrentUser {
        Long userId;
        Long coupleId; // 커플 ID (연동되지 않은 경우 null)

        public CurrentUser(Long userId, Long coupleId) {
            this.userId = userId;
            this.coupleId = coupleId;
        }
    }


    /**
     * [POST] /api/v1/photos
     * 새로운 사진을 업로드하는 API 엔드포인트.
     * multipart/form-data 형식으로 이미지 파일과 기타 정보를 받습니다.
     *
     * @param file      업로드할 이미지 파일 (필수)
     * @param caption   사진 설명 (선택 사항)
     * @param takenDate 사진 촬영 날짜 (선택 사항, "YYYY-MM-DD" 형식)
     * @return 업로드된 사진 정보 DTO (PhotoResponse)
     */
    @Operation(
            summary = "추억 사진 업로드",
            description = "이미지 파일(file)과 사진 설명(caption), 촬영일(takenDate)을 받아 S3 버킷에 저장하고 앨범에 등록합니다.<br>" +
                    "**주의:** 커플 연동이 완료된 유저만 업로드할 수 있습니다. (Multipart 형식 요청)"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "takenDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate takenDate
    ) {
        CurrentUser currentUser = getCurrentUser(); // 현재 로그인 사용자 정보 추출

        // 커플 연동이 안 된 사용자는 사진 업로드 불가
        if (currentUser.coupleId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null); // 403 Forbidden
        }

        try {
            PhotoResponse response = photoService.uploadPhoto(file, currentUser.coupleId, currentUser.userId, caption);
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
        } catch (IOException e) {
            // S3 업로드 실패 등 IO 관련 에러
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build(); // 500 Internal Server Error
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * [GET] /api/v1/photos
     * 현재 로그인한 사용자의 커플이 업로드한 모든 사진 목록을 조회하는 API 엔드포인트.
     *
     * @return 사진 목록 DTO (List<PhotoResponse>)
     */
    @Operation(
            summary = "우리 커플 앨범 전체 조회",
            description = "현재 로그인한 유저가 속한 커플 앨범의 모든 사진 목록을 최신순으로 가져옵니다."
    )
    @GetMapping
    public ResponseEntity<List<PhotoResponse>> getCouplePhotos() {
        CurrentUser currentUser = getCurrentUser(); // 현재 로그인 사용자 정보 추출

        // 커플 연동이 안 된 사용자는 사진 조회 불가
        if (currentUser.coupleId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build(); // 403 Forbidden
        }

        List<PhotoResponse> photos = photoService.getPhotosByCoupleId(currentUser.coupleId);
        return ResponseEntity.ok(photos); // 200 OK
    }

    /**
     * [DELETE] /api/v1/photos/{photoId}
     * 특정 사진을 삭제하는 API 엔드포인트.
     * 사진을 올린 본인만 삭제할 수 있도록 검증 로직이 포함됩니다.
     *
     * @param photoId 삭제할 사진의 고유 ID
     * @return HTTP 상태 코드 (204 No Content, 403 Forbidden, 404 Not Found, 500 Internal Server Error)
     */
    @Operation(
            summary = "앨범 사진 삭제 ️",
            description = "지정한 사진 고유 ID(photoId)를 삭제합니다. 사진을 직접 올린 본인만 삭제 권한이 있습니다."
    )
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        CurrentUser currentUser = getCurrentUser(); // 현재 로그인 사용자 정보 추출

        // 커플 연동이 안 된 사용자는 사진 삭제 불가
        if (currentUser.coupleId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build(); // 403 Forbidden
        }

        try {
            photoService.deletePhoto(photoId, currentUser.coupleId, currentUser.userId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            // Service 계층에서 발생한 비즈니스 로직 관련 예외 처리
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
            } else { // "삭제 권한이 없습니다", "사진을 올린 본인만 삭제할 수 있습니다" 등의 메시지
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }
        } catch (RuntimeException e) {
            // 그 외 예상치 못한 런타임 에러 (S3 삭제 실패 등)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}
