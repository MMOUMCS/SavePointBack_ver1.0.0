package com.couple.gallery.couple_gallery_backend.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long coupleId;

    @Column(nullable = false)
    private String process; // ex) "notepad.exe"

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Column
    private Long durationMinutes; // 종료 시 계산해서 저장

    // ─── 생성자 ───────────────────────────────────────
    public GameSession() {}

    public GameSession(Long userId, Long coupleId, String process) {
        this.userId = userId;
        this.coupleId = coupleId;
        this.process = process;
        this.startedAt = LocalDateTime.now();
    }

    // ─── 종료 처리 ────────────────────────────────────
    public void finish() {
        this.endedAt = LocalDateTime.now();
        this.durationMinutes = java.time.Duration
            .between(this.startedAt, this.endedAt)
            .toMinutes();
    }

    // ─── Getter ───────────────────────────────────────
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCoupleId() { return coupleId; }
    public String getProcess() { return process; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public Long getDurationMinutes() { return durationMinutes; }
}
