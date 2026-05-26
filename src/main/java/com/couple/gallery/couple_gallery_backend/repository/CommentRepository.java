package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 사진 ID에 해당하는 댓글들을 작성 시간 내림차순으로 조회
    List<Comment> findByPhotoIdOrderByCreatedAtDesc(Long photoId);
}