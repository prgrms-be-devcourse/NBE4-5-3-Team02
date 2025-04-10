package com.snackoverflow.toolgether.domain.user.dto.response;

import com.snackoverflow.toolgether.domain.user.entity.User;

import java.time.LocalDateTime;

data class MeInfoResponse(
    val id: Long,
    val nickName: String,
    val email: String,
    val profileImage: String?,
    val phoneNumber: String,
    val baseAddress: String,
    val createdAt: LocalDateTime,
    val score: Int,
    val credit: Int
) {
    companion object {
        @JvmStatic
        fun from(user: User): MeInfoResponse {
            return MeInfoResponse(
                user.id!!,
                user.nickname!!,
                user.profileImage!!,
                user.email!!,
                user.phoneNumber!!,
                user.baseAddress!!,
                user.createdAt!!,
                user.score,
                user.credit
            )
        }
    }
}
