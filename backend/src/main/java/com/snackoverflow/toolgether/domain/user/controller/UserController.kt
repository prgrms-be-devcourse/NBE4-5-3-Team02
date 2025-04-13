package com.snackoverflow.toolgether.domain.user.controller

import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserServiceV2
) {

    @GetMapping("/profile")
    fun getProfile(@Login customUserDetails: CustomUserDetails): RsData<String> {
        val userId = customUserDetails.userId
        val profile = userService.getMyProfile(userId)
        return RsData(
            "200-1",
            "프로필 조회 성공",
            profile
        )
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): RsData<User> {
        val user = userService.getUserById(id)
        return RsData(
            "200-2",
            "$id 번 유저 검색",
            user
        )
    }
}