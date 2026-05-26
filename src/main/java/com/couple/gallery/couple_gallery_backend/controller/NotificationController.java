package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM 푸시 알림 API", description = "기기 토큰을 등록하고, 5가지 상황(콕 찌르기, 게임 초대, 플레이 요청, 사진, 리플레이)에 맞는 푸시 알림을 발송합니다.")
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 토큰 저장용 API
    @Operation(
            summary = "FCM 기기 토큰 등록/저장",
            description = "사용자의 이메일(email)과 해당 기기의 FCM 토큰(token)을 매칭하여 서버에 저장합니다. 푸시 알림을 받기 위한 필수 단계입니다."
    )
    @PostMapping("/token")
    public ResponseEntity<String> saveToken(@RequestParam("email") String email, @RequestParam("token") String token) {
        notificationService.saveToken(email, token);
        return ResponseEntity.ok("토큰 저장 완료");
    }

    // 5가지 알림 통합 발송 API
    @Operation(
            summary = "5가지 커플 알림 통합 발송",
            description = "요청 바디의 타입(type)에 따라 맞춤형 푸시 알림을 상대방에게 보냅니다.<br><br>" +
                    "**[타입별 필요 데이터 가이드]**<br>" +
                    "• `PING` : 이메일 필수 (상대방 콕 찌르기)<br>" +
                    "• `INVITE` : 이메일, 게임 이름(gameName) 필수<br>" +
                    "• `REQUEST` : 이메일, 시간(time), 게임 이름(gameName) 필수 (같이 하자고 졸라대기)<br>" +
                    "• `IMAGE` : 이메일 필수 (추억 사진 업로드 알림)<br>" +
                    "• `REPLAY` : 이메일 필수 (다시보기 알림)"
    )
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody FcmRequestDto dto) {
        String type = dto.getType().toUpperCase();

        switch (type) {
            case "PING":
                notificationService.sendPing(dto.getEmail());
                break;
            case "INVITE":
                notificationService.sendInvite(dto.getEmail(), dto.getGameName()); //  여기서 dto.getGameName()을 정상 호출합니다.
                break;
            case "REQUEST":
                notificationService.sendRequest(dto.getEmail(), dto.getTime(), dto.getGameName());
                break;
            case "IMAGE":
                notificationService.sendImageNotification(dto.getEmail());
                break;
            case "REPLAY":
                notificationService.sendReplayNotification(dto.getEmail());
                break;
            default:
                return ResponseEntity.badRequest().body("알 수 없는 알림 타입: " + type);
        }

        return ResponseEntity.ok("[" + type + "] 알림 발송 완료");
    }

    //  변수 누락 오류를 방지하기 위해 확실하게 선언된 DTO 클래스
    @Data
    static
    class FcmRequestDto {
        private String email;    // 보내는 사람 이메일 (이름 대신 이메일 검색 유지를 위해 필수!)
        private String type;     // PING, INVITE, REQUEST, IMAGE, REPLAY
        private String gameName; // 게임 이름
        private String time;     // 약속 시간
    }
}

