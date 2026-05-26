package com.couple.gallery.couple_gallery_backend.dto;

import com.couple.gallery.couple_gallery_backend.domain.Replay;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReplayResponse {
    private Long replayId;
    private Long coupleId;
    private String youtubeVideoId;
    private String title;
    private String uploaderName;
    private Long viewCount;
    private Long likeCount;
    private LocalDateTime createdAt;

    public static ReplayResponse from(Replay replay) {
        return ReplayResponse.builder()
                .replayId(replay.getId())
                .coupleId(replay.getCoupleId())
                .youtubeVideoId(replay.getYoutubeVideoId())
                .title(replay.getTitle())
                .uploaderName(replay.getUploaderName())
                .viewCount(replay.getViewCount())
                .likeCount(replay.getLikeCount())
                .createdAt(replay.getCreatedAt())
                .build();
    }
}