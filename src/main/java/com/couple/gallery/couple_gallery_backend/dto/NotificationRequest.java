package com.couple.gallery.couple_gallery_backend.dto;

// 5가지 알림 쏠 때 통용해서 쓰는 데이터 껍데기
import lombok.Data;

@Data
public class NotificationRequest {
    private Long senderId;     // 보내는 사람 ID
    private Long receiverId;   // 받는 사람 ID
    private String type;       // 알림 종류 (PING, INVITE, REQUEST, IMAGE, REPLAY)
    private String gameName;   // [선택] 초대 시 게임 이름
    private String time;       // [선택] 약속 시 시간 (예: "10시")
}