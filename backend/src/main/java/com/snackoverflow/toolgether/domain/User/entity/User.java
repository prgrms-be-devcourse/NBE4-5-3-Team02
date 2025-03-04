package com.snackoverflow.toolgether.domain.User.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import com.snackoverflow.toolgether.domain.Address;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private String username; // 사용자 ID (일반 사용자)

    @Column(nullable = true)
    private String password; // 암호화된 비밀번호 (일반 사용자)

    @Column(nullable = true)
    private String providerId; // 소셜 로그인 사용자 ID

    @Column(nullable = true)
    private String provider; // 소셜 로그인 제공자

    @Column(unique = true)
    private String phoneNumber; // 전화번호

    @Column(unique = true)
    private String nickname; // 사용자 별명

    @Embedded
    private Address address; // 주소

    @CreatedDate
    private LocalDateTime createdAt; // 가입 일자

    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도

    private String profileImage; // 사용자 프로필 이미지, uuid로 저장

    @Builder.Default
    private int score = 30; // 유저 평가 정보: 기본값 30점

    @Builder.Default
    private int credit = 0; // 보증금 환불 필드
}
