package com.snackoverflow.toolgether.domain.user.dto;

import com.snackoverflow.toolgether.domain.user.entity.User;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record MeInfoResponse(
        @NonNull Long id,
        @NonNull String nickname,
        String username,
        String profileImage,
        String email,
        @NonNull String phoneNumber,
        @NonNull AddressInfo address,
        @NonNull LocalDateTime createdAt,
        @NonNull Integer score,
        @NonNull Integer credit
) {
    public static MeInfoResponse from(User user) {
        return new MeInfoResponse(
                user.getId(),
                user.getNickname(),
                user.getUsername(),
                user.getProfileImage(),
                user.getEmail(),
                user.getPhoneNumber(),
                AddressInfo.from(user.getAddress()),
                user.getCreatedAt(),
                user.getScore(),
                user.getCredit()
        );
    }
}
