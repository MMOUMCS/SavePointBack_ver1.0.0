package com.couple.gallery.couple_gallery_backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    // 상대방 폰(토큰)으로 푸시 쏘는 메서드
    public void sendMessage(String targetToken, String title, String body) {
        try {
            // 알림 내용 조립
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 대상 지정해서 메시지 만들기
            Message message = Message.builder()
                    .setToken(targetToken) // 상대방 폰의 고유 토큰 (나중에 DB에서 가져올 거야)
                    .setNotification(notification)
                    .build();

            // 전송!
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("푸시 전송 성공: " + response);

        } catch (Exception e) {
            System.err.println("푸시 쏘다가 에러 남... : " + e.getMessage());
        }
    }
}