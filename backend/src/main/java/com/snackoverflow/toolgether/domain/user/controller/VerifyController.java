package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.v2.SmsVerifyRequest;
import com.snackoverflow.toolgether.domain.user.service.MessageService;
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2;
import com.snackoverflow.toolgether.global.dto.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/users")
public class VerifyController {

    private final MessageService messageService;
    private final UserServiceV2 userService;

    public VerifyController(MessageService messageService, UserServiceV2 userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    // 휴대폰으로 인증 번호 전송
    @PostMapping("/send")
    public RsData<?> sendSms(@RequestBody String phoneNumber) {
        // 인증 번호 전송과 함께 Redis 에 인증 코드를 저장
        messageService.sendVerificationCodeAndSave(phoneNumber);
        return new RsData<>(
                "201",
                "인증 번호가 전송되었습니다.",
                Map.of("expired_in", 300)); // 인증 번호 만료 시간 전송
    }

    // 인증 번호 검증
    @PostMapping("/verify")
    public RsData<?> verifySms(@RequestBody SmsVerifyRequest request) {
        messageService.verifyCode(request.getPhoneNumber(), request.getCode());
        return new RsData<>("200",
                "휴대폰 인증에 성공하였습니다.",
                true);
    }


    // 중복 실시간 검증 (이메일)
    @PostMapping("/check-username")
    public RsData<?> checkEmail(@RequestParam String username) {
        userService.checkEmailDuplicate(username);
        return new RsData<>("201",
                "사용 가능한 이메일입니다.",
                Map.of("username", true));
    }

    // 중복 실시간 검증 (닉네임)
    @PostMapping("/check-nickname")
    public RsData<?> checkNickname(@RequestParam String nickname) {
        userService.checkNicknameDuplicate(nickname);
        return new RsData<>("201",
                "사용 가능한 닉네임입니다.",
                Map.of("nickname", true));
    }
}
