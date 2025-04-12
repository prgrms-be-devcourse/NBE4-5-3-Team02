package com.snackoverflow.toolgether.domain.user.controller

import com.snackoverflow.toolgether.domain.user.dto.v2.SmsVerifyRequest
import com.snackoverflow.toolgether.domain.user.service.MessageService
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2
import com.snackoverflow.toolgether.global.dto.RsData
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/users")
class VerifyController(
    private val messageService: MessageService,
    private val userService: UserServiceV2
) {

    // 휴대폰으로 인증 번호 전송
    @PostMapping("/send")
    fun sendSms(@RequestBody phoneNumber: String
    ): RsData<Any> {

        // 인증 번호 전송과 함께 Redis에 인증 코드를 저장
        messageService.sendVerificationCodeAndSave(phoneNumber)

        return RsData(
            resultCode = "201",
            msg = "인증 번호가 전송되었습니다.",
            data = mapOf("expired_in" to 300) // 인증 번호 만료 시간 전송
        )
    }

    // 인증 번호 검증
    @PostMapping("/verify")
    fun verifySms(@RequestBody request: SmsVerifyRequest
    ): RsData<Any> {

        messageService.verifyCode(request.phoneNumber, request.code)

        return RsData(
            resultCode = "200",
            msg = "휴대폰 인증에 성공하였습니다.",
            data = true
        )
    }

    // 중복 실시간 검증 (이메일)
    @PostMapping("/check-username")
    fun checkEmail(@RequestParam username: String
    ): RsData<Any> {

        userService.checkEmailDuplicate(username)

        return RsData(
            resultCode = "201",
            msg = "사용 가능한 이메일입니다.",
            data = mapOf("username" to true)
        )
    }

    // 중복 실시간 검증 (닉네임)
    @PostMapping("/check-nickname")
    fun checkNickname(@RequestParam nickname: String
    ): RsData<Any> {

        userService.checkNicknameDuplicate(nickname)

        return RsData(
            resultCode = "201",
            msg = "사용 가능한 닉네임입니다.",
            data = mapOf("nickname" to true)
        )
    }
}