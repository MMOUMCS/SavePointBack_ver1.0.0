package com.couple.gallery.couple_gallery_backend.dto;

import com.couple.gallery.couple_gallery_backend.domain.Photo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PhotoResponse {
    private Long id;
    private Long coupleId;
    private Long uploaderId;
    private String s3Key; // 디버깅/참조용
    private String imageUrl; // S3 Pre-signed URL
    private String caption;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private Integer commentCount;

    // Entity -> DTO 변환 메서드 (S3Service에서 Pre-signed URL을 받아와서 주입)
    public static PhotoResponse from(Photo photo, String imageUrl, String thumbnailUrl) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .coupleId(photo.getCoupleId())
                .uploaderId(photo.getUploaderId())
                .s3Key(photo.getS3Key())
                .imageUrl(imageUrl)
                .caption(photo.getCaption())
                .createdAt(photo.getCreatedAt())
                .thumbnailUrl(thumbnailUrl)
                .commentCount(photo.getCommentCount())
                .build();
    }

}