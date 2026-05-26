package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    // FCM 토큰 저장 (이메일 기준)
    @Transactional
    public void saveToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    // 파트너 찾기 공통 메서드 (이메일 기준)
    private User getPartner(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        return userRepository.findByCoupleIdAndIdNot(user.getCoupleId(), user.getId())
                .orElseThrow(() -> new RuntimeException("파트ナー 없음"));
    }

    // 1. Ping
    public void sendPing(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        User partner = getPartner(email);

        fcmService.sendMessage(
                partner.getFcmToken(),
                "⚡ 콕 찌르기!",
                user.getUsername() + "님이 '뭐해?' 하고 콕 찔렀어요!" //  getEmail -> getUsername으로 변경!
        );
    }

    // 2. Invite
    public void sendInvite(String email, String gameName) {
        User user = userRepository.findByEmail(email).orElseThrow();
        User partner = getPartner(email);

        fcmService.sendMessage(
                partner.getFcmToken(),
                "🎮 게임 초대장",
                user.getUsername() + "님이 지금 " + gameName + " 같이 하자고 손짓하네요!"
        );
    }

    // 3. Request (개큰문제 완벽 해결 버전)
    public void sendRequest(String email, String time, String gameName) {
        User user = userRepository.findByEmail(email).orElseThrow();
        User partner = getPartner(email);

        fcmService.sendMessage(
                partner.getFcmToken(),
                "⏰ 게임 약속",
                user.getUsername() + "님이 " + time + "에 " + gameName + " 한판 해요!"
        );
    }

    // 4. 이미지 업로드 알림
    public void sendImageNotification(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        User partner = getPartner(email);

        fcmService.sendMessage(
                partner.getFcmToken(),
                "📸 새로운 추억 도착",
                user.getUsername() + "님이 새로운 사진을 등록했어요. 지금 확인해보세요!"
        );
    }

    // 5. 리플레이 업로드 알림
    public void sendReplayNotification(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        User partner = getPartner(email);

        fcmService.sendMessage(
                partner.getFcmToken(),
                "🎬 리플레이 업데이트",
                user.getUsername() + "님이 새로운 리플레이를 게시했습니다!"
        );
    }
}