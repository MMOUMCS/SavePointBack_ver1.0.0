package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.Replay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReplayRepository extends JpaRepository<Replay, Long> {
    // 커플 영상을 최신순으로 가져옴
    List<Replay> findByCoupleIdOrderByCreatedAtDesc(Long coupleId);
    // 우리 커플것인지 확인함
    Optional<Replay> findByIdAndCoupleId(Long id, Long coupleId);
}