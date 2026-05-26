package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {
    // user1Id 또는 user2Id 중 하나가 일치하는 Couple을 찾습니다.
    Optional<Couple> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
}