package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.config.security.UserDetailsImpl;
import com.couple.gallery.couple_gallery_backend.domain.Couple;
import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.repository.CoupleRepository;
import com.couple.gallery.couple_gallery_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Couple connectCouple(String targetEmail, String connectionCode) {

        // 1. 현재 인증된 사용자(나)와 대상 사용자를 DB에서 조회 (기존 로직)
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        User me = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));
        User target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException("연결할 상대방 이메일을 찾을 수 없습니다."));

        // 2.  새로운 검증: 상대방의 connectionCode 일치 여부 확인
        //    - 상대방이 connectionCode를 설정했는지 확인
        String encryptedCode = target.getConnectionCode();
        if (encryptedCode == null || !passwordEncoder.matches(connectionCode, encryptedCode)) {
            throw new IllegalArgumentException("커플 연결 코드가 일치하지 않거나 설정되지 않았습니다.");
        }

        // 3. 기존 비즈니스 규칙 검증 (자신과 연결, 이미 연결 여부)
        if (me.getEmail().equals(target.getEmail())) {
            throw new IllegalStateException("자기 자신과 커플을 연결할 수 없습니다.");
        }
        if (me.getCoupleId() != null || target.getCoupleId() != null) {
            throw new IllegalStateException("이미 커플이 연결된 사용자입니다.");
        }

        // 4. 새로운 Couple 엔티티 생성 및 저장 (기존 로직)
        Couple newCouple = Couple.builder()
                .user1Id(me.getId())
                .user2Id(target.getId())
                .status(Couple.CoupleStatus.ACTIVE)
                .build();

        Couple savedCouple = coupleRepository.save(newCouple);
        Long coupleId = savedCouple.getId();

        // 5. 두 사용자의 couple_id 업데이트 (기존 로직)
        me.setCoupleId(coupleId);
        target.setCoupleId(coupleId);

        // 6. 연결 성공 후, 사용된 코드는 Null 처리하여 재사용을 막거나 초기화
        target.setConnectionCode(null);
        // Note: me.setConnectionCode(null)은 me가 초대 코드를 사용하지 않았다면 불필요함.
        // 이 로직에서는 target이 코드를 제공했으므로 target의 코드만 초기화

        userRepository.save(me);
        userRepository.save(target);

        return savedCouple;
    }

    /**
     * 현재 인증된 사용자가 커플인지 확인하는 편의 메서드
     */
    public boolean isUserCoupled(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.isPresent() && userOpt.get().getCoupleId() != null;
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정하여 성능 최적화
    public Couple findCoupleByUserId(Long userId) {
        // CoupleRepository의 findByUser1IdOrUser2Id 메서드를 사용하여 커플을 찾습니다.
        // 이 메서드는 user1Id 또는 user2Id가 주어진 userId와 일치하는 경우를 찾습니다.
        return coupleRepository.findByUser1IdOrUser2Id(userId, userId)
                .orElse(null); // 커플을 찾지 못하면 null을 반환합니다.
    }

    // CoupleService.java 내부
    @Transactional(readOnly = true)
    public User getPartnerInfo(Long myId) {
        // 내가 속한 커플 정보를 찾음
        Couple couple = coupleRepository.findByUser1IdOrUser2Id(myId, myId)
                .orElseThrow(() -> new IllegalStateException("연결된 커플 정보가 없습니다."));

        // 내가 User1이면 상대방은 User2, 내가 User2이면 상대방은 User1
        Long partnerId = couple.getUser1Id().equals(myId) ? couple.getUser2Id() : couple.getUser1Id();

        return userRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("상대방이 존재하지 않습니다."));
    }
}