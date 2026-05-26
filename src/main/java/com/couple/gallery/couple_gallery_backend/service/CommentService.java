package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.domain.Comment;
import com.couple.gallery.couple_gallery_backend.domain.Photo;
import com.couple.gallery.couple_gallery_backend.dto.CommentRequest;
import com.couple.gallery.couple_gallery_backend.dto.CommentResponse;
import com.couple.gallery.couple_gallery_backend.repository.CommentRepository;
import com.couple.gallery.couple_gallery_backend.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository; // PhotoService와 공유

    // 1. 댓글 작성
    @Transactional
    public CommentResponse createComment(Long photoId, Long userId, CommentRequest request) {

        // 1-1. 사진 유효성 검사 (존재 여부 확인 및 커플 ID 확인)
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));

        // 1-2. 댓글 엔티티 생성 및 저장
        Comment comment = Comment.builder()
                .photoId(photoId)
                .userId(userId)
                .content(request.getContent())
                .build();
        Comment savedComment = commentRepository.save(comment);

        // 1-3. 캐싱 카운터 업데이트
        photo.setCommentCount(photo.getCommentCount() + 1);
        photoRepository.save(photo);

        return CommentResponse.from(savedComment);
    }

    // 2. 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPhotoId(Long photoId) {
        List<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);

        return comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    // 3. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 3-1. 삭제 권한 확인 (본인만 삭제 가능)
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        // 3-2. 사진의 카운터 업데이트
        Photo photo = photoRepository.findById(comment.getPhotoId())
                .orElseThrow(() -> new IllegalArgumentException("사진 정보를 찾을 수 없습니다."));

        photo.setCommentCount(photo.getCommentCount() - 1);
        photoRepository.save(photo);

        // 3-3. 댓글 삭제
        commentRepository.delete(comment);
    }
}