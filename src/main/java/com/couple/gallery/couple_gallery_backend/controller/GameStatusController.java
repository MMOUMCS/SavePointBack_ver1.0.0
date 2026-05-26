package com.couple.gallery.couple_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "게임 실시간 상태 API", description = "일렉트론 데스크톱 앱으로부터 게임 구동 상태를 수신하고, 앱에 현재 상태를 공유하는 API입니다.")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Electron 로컬 요청 허용
public class GameStatusController {

    // ─── 수신 DTO ─────────────────────────────────────
    public static class GameStatusRequest {
        public boolean running;
        public String process;   // ex) "notepad.exe"
        public String timestamp; // ISO 8601
    }

    // ─── 마지막 상태 (메모리 저장 — DB 연동 시 Service로 이동) ──
    private GameStatusRequest lastStatus;

    // ─── POST /api/game-status  (Electron → Spring) ───
    @Operation(
            summary = "데스크톱 상태 수신 (Electron -> Spring)",
            description = "일렉트론 백그라운드 프로그램이 현재 특정 게임 프로세스가 실행 중(running: true)인지 종료되었는지 실시간으로 백엔드에 보고합니다."
    )
    @PostMapping("/game-status")
    public ResponseEntity<String> receiveStatus(@RequestBody GameStatusRequest req) {
        lastStatus = req;

        String msg = req.running
            ? "[GAME] detected: " + req.process
            : "[GAME] process gone -- idle";
        System.out.println("[" + Instant.now() + "] " + msg);

        return ResponseEntity.ok("ok");
    }

    // ─── GET /api/game-status  (앱 → Spring 폴링용) ───
    @Operation(
            summary = "현재 게임 구동 상태 조회 (앱 -> 폴링용)",
            description = "현재 유저가 게임 중인지 실시간으로 상태를 조회합니다. 데이터가 없으면 실행 중이지 않은 공백 상태를 반환합니다."
    )
    @GetMapping("/game-status")
    public ResponseEntity<GameStatusRequest> getStatus() {
        if (lastStatus == null) {
            GameStatusRequest empty = new GameStatusRequest();
            empty.running = false;
            empty.process = null;
            empty.timestamp = Instant.now().toString();
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(lastStatus);
    }
}
