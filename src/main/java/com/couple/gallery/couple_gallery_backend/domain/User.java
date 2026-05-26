package com.couple.gallery.couple_gallery_backend.domain;

import jakarta.persistence.*; // JPA 관련 어노테이션 임포트
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.*;

@Entity // 이 클래스가 DB의 테이블임을 알리는 가장 중요한 어노테이션
@Table(name = "users") // DB 테이블 이름을 'users'로 지정
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id // 이 필드가 테이블의 기본 키(Primary Key)임을 명시
    @GeneratedValue(strategy = GenerationType.IDENTITY) //  값 자동 증가 설정 (PostgreSQL의 BIGSERIAL)
    private Long id; // 사용자 고유 ID

    @Column(unique = false, nullable = false, length = 50)
    private String username; // 로그인에 사용할 사용자 이름

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(name = "couple_id", nullable = true)
    private Long coupleId;

    @Column(name = "connection_code", nullable = true)
    private String connectionCode;

    @Column(nullable = true)
    private String profileImageUrl;

    @Column(nullable = true)
    private String fcmToken;
}