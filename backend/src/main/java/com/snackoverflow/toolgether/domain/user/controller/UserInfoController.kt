package com.snackoverflow.toolgether.domain.user.controller

import com.snackoverflow.toolgether.domain.user.dto.request.EmailRequest
import com.snackoverflow.toolgether.domain.user.dto.request.VerificationData
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2
import com.snackoverflow.toolgether.domain.user.service.VerificationService
import com.snackoverflow.toolgether.global.constants.AppConstants.SESSION_KEY
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/users")
class UserInfoController(
    @Value("\${custom.site.frontUrl}") private val frontUrl: String,
    private val userService: UserServiceV2,
    private val verificationService: VerificationService,
    private val log: Logger
) {

    // 이메일 찾기 -> 휴대폰 인증 send, verify
    @PostMapping("/find-email")
    fun findEmail(
        @RequestParam phoneNumber: String
    ): RsData<Any> {

        val email = userService.getUserEmail(phoneNumber)
        return RsData("200", "고객님의 이메일: $email", email)
    }

    // 비밀번호 변경 -> 이메일 인증 확인 링크 전송
    @PostMapping("/send-verification")
    fun sendVerification(
        @Validated @RequestBody request: EmailRequest,
        session: HttpSession
    ): RsData<Any> {

        verificationService.sendEmailWithCode(request.email, session)
        return RsData("200-1", "인증 메일이 발송되었습니다.")
    }

    // 이메일 인증 확인
    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam("code") code: String,
        session: HttpSession,
        response: HttpServletResponse
    ): RsData<Any> {

        val data = session.getAttribute(SESSION_KEY) as? VerificationData
        return if (data != null && data.code == code) {

            // 인증 성공 처리
            log.info("이메일 링크 인증 성공 - 세션 정보: {}, 입력 정보:{}", data.code, code)
            data.verified = true
            session.setAttribute(SESSION_KEY, data)
            response.sendRedirect("$frontUrl/success")

            RsData("201", "이메일 인증에 성공하였습니다.")
        } else {
            response.sendRedirect("$frontUrl/fail")

            RsData("400", "이메일 인증에 실패했습니다.")
        }
    }

    // 비밀번호 변경
    data class PasswordRequest(val password: String)

    @PostMapping("/change-password")
    fun changePassword(
        @Login userDetails: CustomUserDetails,
        @RequestBody password: PasswordRequest
    ): RsData<Any> {

        // 이전 패스워드와 같은 걸로는 변경할 수 없음
        userService.checkBeforePassword(userDetails.userId, password.password)

        // 패스워드 변경
        userService.changePassword(userDetails.userId, password.password)

        return RsData("200", "비밀번호 변경에 성공했습니다")
    }
}
