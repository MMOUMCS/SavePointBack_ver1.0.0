package com.couple.gallery.couple_gallery_backend.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "couples")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 커플 고유 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime connectedAt;

    @Column(unique = true)
    private String connectionCode;

    @Column(name = "user1id", nullable = false)
    private Long user1Id; // 첫 번째 사용자 ID (FK)

    @Column(name = "user2id",nullable = false)
    private Long user2Id; // 두 번째 사용자 ID (FK)

    public enum CoupleStatus {
        ACTIVE,         // 활성 상태 (연결 완료)
        PENDING,        // 대기 중 (초대 시스템 사용 시)
        DISCONNECTED    // 연결 해제
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CoupleStatus status = CoupleStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        if (connectedAt == null) {
            connectedAt = LocalDateTime.now();
        }
    }
}