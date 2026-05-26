package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.config.security.UserDetailsImpl;
import com.couple.gallery.couple_gallery_backend.dto.CommentRequest;
import com.couple.gallery.couple_gallery_backend.dto.CommentResponse;
import com.couple.gallery.couple_gallery_backend.repository.UserRepository;
import com.couple.gallery.couple_gallery_backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.couple.gallery.couple_gallery_backend.domain.User;

import jakarta.validation.Valid;

import java.util.List;

@Tag(name = "사진 댓글 API", description = "사진에 댓글을 달고, 조회하고, 삭제하는 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photos/{photoId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    // 1. 댓글 작성 (POST /api/v1/photos/{photoId}/comments)
    @Operation(
            summary = "사진 댓글 작성",
            description = "지정한 사진 ID(photoId)에 새로운 댓글을 등록합니다."
    )
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long photoId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // 1. 파라미터로 주입 받기

        // 2. 이메일 추출
        String email = userDetails.getUsername();

        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow();

        CommentResponse response = commentService.createComment(photoId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 댓글 조회 (GET /api/v1/photos/{photoId}/comments)
    @Operation(
            summary = "사진별 댓글 전체 조회",
            description = "특정 사진 ID(photoId)에 달린 모든 댓글 목록을 실시간으로 가져옵니다."
    )
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long photoId) {

        List<CommentResponse> responses = commentService.getCommentsByPhotoId(photoId);

        return ResponseEntity.ok(responses);
    }

    // 3. 댓글 삭제 (DELETE /api/v1/photos/{photoId}/comments/{commentId})
    @Operation(
            summary = "댓글 삭제",
            description = "지정한 댓글 ID(commentId)를 삭제합니다. 자신이 작성한 댓글만 삭제할 수 있습니다."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        Long userId = principal.getUserId();
        commentService.deleteComment(commentId, userId);

        return ResponseEntity.noContent().build();
    }
}