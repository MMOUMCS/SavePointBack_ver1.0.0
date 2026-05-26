package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.domain.GameSession;
import com.couple.gallery.couple_gallery_backend.service.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "게임 세션 및 통계 API", description = "유저 및 커플별 미니게임 플레이 기록과 총 플레이 시간 통계 데이터를 제공합니다.")
@RestController
@RequestMapping("/api/game-sessions")
public class GameSessionController {

    private final GameSessionService service;

    public GameSessionController(GameSessionService service) {
        this.service = service;
    }

    // 특정 유저 세션 기록 조회
    // GET /api/game-sessions/user/15
    @Operation(
            summary = "개인 게임 기록 조회",
            description = "특정 유저 고유 ID(userId)를 기반으로 해당 유저가 플레이했던 모든 게임 세션 기록 리스트를 가져옵니다."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GameSession>> getUserSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getSessionsByUser(userId));
    }

    // 커플 전체 세션 기록 조회
    // GET /api/game-sessions/couple/5
    @Operation(
            summary = "우리 커플 게임 기록 전체 조회",
            description = "우리 커플 ID(coupleId)로 묶인 공동의 게임 플레이 기록들을 싹 다 불러옵니다."
    )
    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<List<GameSession>> getCoupleSessions(@PathVariable Long coupleId) {
        return ResponseEntity.ok(service.getSessionsByCouple(coupleId));
    }

    // 게임별 총 플레이 시간
    // GET /api/game-sessions/user/15/playtime
    @Operation(
            summary = "게임별 총 플레이 시간 통계",
            description = "유저 ID(userId)를 기준으로 어떤 게임(process)을 총 몇 분(totalMinutes) 동안 플레이했는지 합산 통계 데이터를 반환합니다."
    )
    @GetMapping("/user/{userId}/playtime")
    public ResponseEntity<List<Map<String, Object>>> getPlayTime(@PathVariable Long userId) {
        List<Object[]> raw = service.getTotalPlayTime(userId);
        List<Map<String, Object>> result = raw.stream().map(row -> Map.of(
            "process", row[0],
            "totalMinutes", row[1]
        )).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
