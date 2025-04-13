package com.snackoverflow.toolgether.domain.user.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import kotlin.apply
import kotlin.run
import kotlin.text.format

@Service
class MailService (
    private val javaMailSender: JavaMailSender
){

    /**
     * 전송자 이메일 application-secret.yml 파일 설정
     * 구글 이용 시 앱 비밀번호 발급 받고 yml 파일 [본인 이메일, 앱 비밀번호] 로 바꿔 주셔야 합니다!
     */
    @Value("\${custom.site.backUrl}")
    private lateinit var BACK_URL: String

    @Value("\${spring.mail.username}")
    private lateinit var SERDER_EMIAL: String

    // 인증 이메일 생성
    fun createMail(email: String, code: String): MimeMessage {
        return javaMailSender.createMimeMessage().apply {
            MimeMessageHelper(this, true, "UTF-8").run {
                setFrom(SERDER_EMIAL)
                setTo(email)
                subject = "Toolgether 비밀번호 변경 - 이메일 인증"
                setText(buildEmailTemplate(code), true) // HTML 컨텐츠 설정
            }
        }
    }

    // 비밀번호 변경 - 인증 이메일 전송 용도
    fun sendMail(prebuiltMessage: MimeMessage) {
        javaMailSender.send(prebuiltMessage)
    }

    fun buildEmailTemplate(code: String): String {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Toolgether 이메일 인증</title>
                </head>
                <body style="margin: 0; padding: 20px; font-family: 'Apple SD Gothic Neo', '맑은 고딕', Arial, sans-serif; background-color: #f4f9fd;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 30px; background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);">
                
                        <!-- 헤더 섹션 -->
                        <header style="text-align: center; padding-bottom: 25px; border-bottom: 2px solid #e0f0e9;">
                            <h1 style="color: #28a745; font-size: 28px; margin: 0 0 15px 0;">
                                🌱 Toolgether 이메일 인증
                            </h1>
                            <p style="font-size: 16px; color: #555; line-height: 1.6; margin: 0;">
                                안녕하세요! <strong style="color: #2c8c4a;">소유에서 공유로</strong>의 가치를 실현하는<br>
                                    Toolgether 팀입니다.<br>
                                🔄 <em>구매 대신 이웃과 공유</em>하는 지속 가능한 라이프스타일을 함께 만들어가요!
                            </p>
                        </header>
                
                        <!-- 본문 섹션 -->
                                       <main style="padding: 25px 0;">
                                           <div style="padding: 25px; background: #eafce4; border-radius: 12px; border: 1px dashed #90d26d;">
                                               <p style="font-size: 14px; color: #4a7c59; margin: 0 0 12px 0;">
                                                   🚨 아래 링크 클릭 후 인증을 완료해 주세요
                                               </p>
                                               <a href="%s/api/v1/users/verify?code=%s"\s
                                                  style="display: inline-block;
                                                         padding: 12px 24px;
                                                         background: #28a745;
                                                         color: white;
                                                         font-size: 18px;
                                                         font-weight: 600;
                                                         text-decoration: none;
                                                         border-radius: 8px;
                                                         transition: background 0.3s ease;
                                                         margin: 15px 0;">
                                                   인증 바로가기
                                               </a>
                                               <div style="font-size: 14px; color: #666; margin-top: 20px;">
                                                   ⏳ 유효 시간: <strong>15분</strong>
                                               </div>
                                           </div>
                                       </main>
                
                        <!-- 푸터 섹션 -->
                        <footer style="margin-top: 30px; text-align: center; color: #888; font-size: 14px;">
                            <hr style="border: none; border-top: 1px solid #e0f0e9; margin: 20px 0;">
                            <p style="margin: 8px 0;">
                                💬 문의:
                                <a href="mailto:support@toolgether.com" 
                                   style="color: #28a745; text-decoration: none; font-weight: 500;">
                                    support@toolgether.com
                                </a>
                            </p>
                            <p style="margin: 8px 0;">
                                ♻️ 2025 Toolgether - 지구를 위한 작은 변화
                            </p>
                        </footer>
                    </div>
                </body>
                </html>
                """.format(BACK_URL, code); // 코드 동적 삽입
    }
}
