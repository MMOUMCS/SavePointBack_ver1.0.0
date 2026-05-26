package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.domain.GameSession;
import com.couple.gallery.couple_gallery_backend.repository.GameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GameSessionService {

    private final GameSessionRepository repository;

    public GameSessionService(GameSessionRepository repository) {
        this.repository = repository;
    }

    // ─── 게임 시작 → DB INSERT (1번) ─────────────────
    @Transactional
    public void startSession(Long userId, Long coupleId, String process) {
        // 혹시 이전 세션이 안 닫혔으면 먼저 닫기
        endSession(userId);

        GameSession session = new GameSession(userId, coupleId, process);
        repository.save(session);
        System.out.println("[SESSION] started: userId=" + userId + ", process=" + process);
    }

    // ─── 게임 종료 → DB UPDATE (1번) ─────────────────
    @Transactional
    public void endSession(Long userId) {
        Optional<GameSession> session =
            repository.findTopByUserIdAndEndedAtIsNullOrderByStartedAtDesc(userId);

        session.ifPresent(s -> {
            s.finish();
            repository.save(s);
            System.out.println("[SESSION] ended: userId=" + userId +
                ", duration=" + s.getDurationMinutes() + "min");
        });
    }

    // ─── 조회 ─────────────────────────────────────────
    public List<GameSession> getSessionsByUser(Long userId) {
        return repository.findByUserIdOrderByStartedAtDesc(userId);
    }

    public List<GameSession> getSessionsByCouple(Long coupleId) {
        return repository.findByCoupleIdOrderByStartedAtDesc(coupleId);
    }

    public List<Object[]> getTotalPlayTime(Long userId) {
        return repository.findTotalPlayTimeByUser(userId);
    }
}
