package com.couple.gallery.couple_gallery_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사진 고유 ID

    @Column(nullable = false)
    private Long coupleId; // 이 사진이 속한 커플 ID (FK)

    @Column(nullable = false)
    private Long uploaderId; // 사진을 올린 사용자 ID (FK)

    @Column(unique = true, nullable = false, length = 512)
    private String s3Key; // AWS S3에 저장된 사진 파일의 고유 URL

    @Column(columnDefinition = "TEXT") // DB에 긴 텍스트를 저장할 때 사용
    private String caption; // 사진 설명

    @Column(nullable = false, length = 512)
    private String thumbnailS3Key;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer commentCount = 0;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();

        if (this.commentCount == null) {
            this.commentCount = 0;
        }
    }
}