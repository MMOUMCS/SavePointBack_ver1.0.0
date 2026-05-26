package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    // 현재 진행 중인 세션 (endedAt이 null인 것)
    Optional<GameSession> findTopByUserIdAndEndedAtIsNullOrderByStartedAtDesc(Long userId);

    // 특정 유저의 전체 세션 기록
    List<GameSession> findByUserIdOrderByStartedAtDesc(Long userId);

    // 특정 커플의 전체 세션 기록
    List<GameSession> findByCoupleIdOrderByStartedAtDesc(Long coupleId);

    // 특정 유저의 게임별 총 플레이 시간
    @Query("SELECT g.process, SUM(g.durationMinutes) FROM GameSession g " +
           "WHERE g.userId = :userId AND g.durationMinutes IS NOT NULL " +
           "GROUP BY g.process ORDER BY SUM(g.durationMinutes) DESC")
    List<Object[]> findTotalPlayTimeByUser(@Param("userId") Long userId);
}
