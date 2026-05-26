package com.couple.gallery.couple_gallery_backend.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "battle_data")
@Getter @Setter
public class BattleData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_name", nullable = false)
    private GameType gameName;

    @Column(nullable = false)
    private String result;

    @Column(name = "play_date", nullable = false)
    private LocalDate playDate;

    @Column(name = "play_time", nullable = false)
    private Integer playTime;

    private Integer kills;
    private Integer deaths;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private LocalDateTime createdAt;

    public enum GameType {
        LOL,        // 리그 오브 레전드
        VALORANT,   // 발로란트
        OVERWATCH,  // 오버워치 2
        FF14,       // 파이널 판타지 14
        ETC         // 기타 게임
    }
}