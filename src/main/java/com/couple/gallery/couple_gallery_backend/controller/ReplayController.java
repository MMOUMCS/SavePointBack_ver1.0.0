package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.config.security.UserDetailsImpl;
import com.couple.gallery.couple_gallery_backend.dto.ReplayRequest;
import com.couple.gallery.couple_gallery_backend.dto.ReplayResponse;
import com.couple.gallery.couple_gallery_backend.service.ReplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "다시보기 영상 API", description = "커플의 추억 영상(리플레이)을 저장, 조회, 삭제하고 좋아요를 누르는 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/replays")
public class ReplayController {

    private final ReplayService replayService;

    // 영상 저장
    @Operation(
            summary = "다시보기 영상 등록",
            description = "우리 커플 앨범에 새로운 다시보기 영상 링크 및 정보를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ReplayResponse> save(
            @RequestBody ReplayRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        Long coupleId = principal.getCoupleId();
        return ResponseEntity.status(HttpStatus.CREATED).body(replayService.save(request, coupleId));
    }

    // 전체 조회
    @Operation(
            summary = "우리 커플 영상 전체 조회",
            description = "현재 로그인한 커플이 공유하고 있는 모든 다시보기 영상 목록을 가져옵니다."
    )
    @GetMapping
    public ResponseEntity<List<ReplayResponse>> findAll(
            @AuthenticationPrincipal UserDetailsImpl principal) {

        Long coupleId = principal.getCoupleId();
        return ResponseEntity.ok(replayService.findAll(coupleId));
    }

    // 단건 조회
    @Operation(
            summary = "영상 상세 단건 조회",
            description = "지정한 영상 고유 ID(id)를 가진 특정 리플레이 영상의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ReplayResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        Long coupleId = principal.getCoupleId();
        return ResponseEntity.ok(replayService.findById(id, coupleId));
    }

    // 좋아요
    @Operation(
            summary = "영상 좋아요 누르기",
            description = "지정한 영상 고유 ID(id)에 좋아요를 누르거나 취소합니다. (커플 간 실시간 반응용)"
    )
    @PostMapping("/{id}/like")
    public ResponseEntity<ReplayResponse> like(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        Long coupleId = principal.getCoupleId();
        return ResponseEntity.ok(replayService.like(id, coupleId));
    }

    // 삭제
    @Operation(
            summary = "영상 삭제",
            description = "지정한 영상 고유 ID(id)를 우리 커플 앨범에서 완전히 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        Long coupleId = principal.getCoupleId();
        replayService.delete(id, coupleId);
        return ResponseEntity.noContent().build();
    }
}