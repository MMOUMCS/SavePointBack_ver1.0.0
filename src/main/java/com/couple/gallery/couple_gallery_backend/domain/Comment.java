package com.couple.gallery.couple_gallery_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter // (엔티티 생성 시 Builder 사용, 수정 시 Setter 사용)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글이 달린 사진의 ID (FK)
    @Column(nullable = false)
    private Long photoId;

    // 댓글을 작성한 사용자 ID (FK)
    @Column(nullable = false)
    private Long userId;

    // 댓글 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}