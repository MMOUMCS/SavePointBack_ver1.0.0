package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // 특정 coupleId의 사진들을 최신순으로 조회 (페이징 가능)
    List<Photo> findByCoupleIdOrderByCreatedAtDesc(Long coupleId);

    // 페이징을 위한 메서드
    // Page<Photo> findByCoupleIdOrderByCreatedAtDesc(Long coupleId, Pageable pageable);
}