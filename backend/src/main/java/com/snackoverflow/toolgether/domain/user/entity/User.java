package com.snackoverflow.toolgether.domain.user.entity;

import com.snackoverflow.toolgether.global.util.Util;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users") // 예약어 변경
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private String username; // 사용자 ID (일반 사용자)

    @Column(nullable = true)
    private String password; // 암호화된 비밀번호 (일반 사용자)

    @Column(nullable = true, unique = true)
    private String email; // 사용자 email (인증 용도)

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

    @Column(nullable = true)
    private Double latitude; // 위도

    @Column(nullable = true)
    private Double longitude; // 경도

    @Column(nullable = true)
    private boolean additionalInfoRequired = true; // 추가 정보 필요 플래그

    @Column(nullable = true)
    private String profileImage; // 사용자 프로필 이미지 링크 저장

    @Builder.Default
    private int score = 30; // 유저 평가 정보: 기본값 30점

    @Builder.Default
    private int credit = 0; // 보증금 환불 필드

    @Column(nullable = true)
    private LocalDateTime deletedAt; // 탈퇴 일자, 탈퇴하면 null이 아님

    public void updateCredit(int credit) {
        this.credit += credit;
    }

    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updateLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public void updateAdditionalInfoRequired(boolean additionalInfoRequired) {
        this.additionalInfoRequired = additionalInfoRequired;
    }

    public void updateAddress(String zipcode, String mainAddress, String detailAddress) {
        this.address = Address.builder()
                .zipcode(zipcode)
                .mainAddress(mainAddress)
                .detailAddress(detailAddress)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String uuid) {
        this.profileImage = uuid;
    }

    public void deleteProfileImage() {
        this.profileImage = null;
    }

    //탈퇴시 호출, 삭제 시간을 기록하고 익명화 진행
    @PreUpdate
    public void preUpdate() {
        if (this.deletedAt != null) {
            anonymize();
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 개인 정보 익명화
    public void anonymize() {
        this.username = "deletedUser-" + Util.generateUUIDMasking();
        this.password = Util.generateUUIDMasking();
        this.email = Util.generateUUIDMasking() + "@deleted.com";
        this.phoneNumber = Util.generateUUIDMasking();
        this.nickname = "삭제된유저-" + Util.generateUUIDMasking();
        this.address = new Address(
                "삭제된 주소",
                "삭제된 상세 주소",
                "00000"
        );
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.profileImage = null;
    }

    public void updateScore(double updatedScore) {
        this.score = (int)Math.round(updatedScore);
    }
}
