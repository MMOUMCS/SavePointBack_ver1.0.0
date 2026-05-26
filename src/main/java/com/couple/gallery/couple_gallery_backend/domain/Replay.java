package com.couple.gallery.couple_gallery_backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Replay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String youtubeVideoId; // 추출한 11자리 ID
    private String title;          // 영상 제목
    private String uploaderName;   // 올린 사람 닉네임
    private Long viewCount = 0L;   // 우리 앱 내 조회수
    private Long coupleId; // 커플 id

    @CreationTimestamp // 데이터가 쌓일 때 현재 시간이 자동으로 들어갑니다.
    private LocalDateTime createdAt;
    private Long likeCount = 0L; // 좋아요 숫자 저장

    // 생성자
    public Replay(String youtubeVideoId, String title, String uploaderName, Long coupleId) {
        this.youtubeVideoId = youtubeVideoId;
        this.title = title;
        this.uploaderName = uploaderName;
        this.coupleId = coupleId;
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 좋아요 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }
}