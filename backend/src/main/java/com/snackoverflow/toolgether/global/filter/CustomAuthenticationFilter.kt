package com.snackoverflow.toolgether.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.domain.user.repository.UserRepository
import com.snackoverflow.toolgether.global.constants.AppConstants.REFRESH_TOKEN
import com.snackoverflow.toolgether.global.constants.AppConstants.REMEMBER_ME_TOKEN
import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException
import com.snackoverflow.toolgether.global.token.JwtService
import com.snackoverflow.toolgether.global.token.TokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CustomAuthenticationFilter(
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(CustomAuthenticationFilter::class.java)

    @Value("\${jwt.access_expiration}")
    private val access: Long = 0;

    @Value("\${jwt.refresh_expiration}")
    private val refresh: Long = 0

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        log.info("CustomAuthenticationFilter 실행")

        try {
            // 테스트 모드 활성화: 헤더에 "X-Test-Auth"가 존재하면 강제 인증 처리
            val testAuthHeader = request.getHeader("X-Test-Auth")
            if (!testAuthHeader.isNullOrBlank()) {
                // 테스트용 사용자 정보 생성
                val testUser = userRepository.findByEmail(testAuthHeader)
                testUser?.id?.let { setAuthentication(testUser.email.toString(), it.toLong()) }
                filterChain.doFilter(request, response) // 필터 체인 계속 진행
                return
            }

            // JWT 토큰 처리
            log.info("header 검증 시작")

            val authorizationHeader = request.getHeader("Authorization")
            log.info("authorizationHeader: {}", authorizationHeader)

            if (!authorizationHeader.isNullOrBlank()) {
                val accessToken = extractTokenFromHeader(request)
                log.info("accessToken 추출: {}", accessToken)

                if (jwtService.isValidToken(accessToken.toString())) {
                    jwtService.parseAndValidateToken(accessToken.toString())?.let { claims ->
                        handleJwt(claims) // 파싱된 Claims 전달
                        filterChain.doFilter(request, response) // 필터 체인 계속 진행
                        return
                    }
                } else {
                    log.info("토큰 만료, 재발급 로직 시작")

                    val newAccessToken = getNewAccessToken(request)
                    response.setHeader("Authorization", "Bearer $newAccessToken") // 재발급 후 다시 헤더에 넣어줄 것!

                    jwtService.parseAndValidateToken(newAccessToken.toString())?.let { newClaims ->
                        handleJwt(newClaims) // 새로운 토큰의 Claims 전달
                        filterChain.doFilter(request, response) // 필터 체인 계속 진행
                        return
                    }
                }

            }

            // Remember-Me 토큰 처리
            jwtService.getTokenByCookieName(request, REMEMBER_ME_TOKEN)?.let { rememberMeToken ->
                checkBlackList(rememberMeToken.toString())

                val claims = jwtService.parseAndValidateToken(rememberMeToken.get())
                val email = claims!!["email"] as String
                val userId = (claims["userId"] as Int).toLong()


                setAuthentication(email, userId)
                saveNewTokens(response, userId, email)

                // 자동 로그인 응답 작성
                val user = userRepository.findByEmail(email)
                response.status = HttpServletResponse.SC_OK // 200 OK
                response.writer.write(getResponse(response, user!!))
                filterChain.doFilter(request, response)
                return // 여기서 체인 종료
            }

            filterChain.doFilter(request, response)

        } catch (e: ExpiredJwtException) {
            getNewAccessToken(request)?.let { newAccessToken ->
                jwtService.parseAndValidateToken(newAccessToken)?.let { newClaims ->
                    handleJwt(newClaims) // 새로운 토큰의 Claims 전달
                } ?: run {
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.writer.write("인증되지 않은 사용자입니다.")
                }
            }
        } catch (e: UserNotFoundException) {
            log.error("유저를 찾을 수 없음: {}", e.message)

            response.status = HttpServletResponse.SC_NOT_FOUND // 404 Not Found
            response.writer.write("사용자를 찾을 수 없음")

        } catch (e: ServiceException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED // 401 Unauthorized
            response.writer.write("인증되지 않은 사용자입니다.")

        } catch (e: Exception) {
            log.error("내부 서버 오류: {}", e.message)

            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR // 500 Internal Server Error
            response.writer.write("서버 오류 발생")
        }
    }

    private fun getResponse(response: HttpServletResponse, user: User): String {
        with(response) {
            contentType = "application/json"
            characterEncoding = "UTF-8"
        }

        val responseBody = mapOf(
            "message" to "자동 로그인 성공",
            "status" to 200,
            "user" to mapOf(
                "userId" to user.id,
                "nickname" to user.nickname
            )
        )

        return objectMapper.writeValueAsString(responseBody)
    }

    private fun extractUserInfoFromClaims(claims: Claims): Pair<String, Long> {
        val email = claims["email"] as String
        val userId = (claims["userId"] as Int).toLong()
        return email to userId
    }

    private fun saveNewTokens(response: HttpServletResponse, userId: Long, email: String) {
        val accessToken = tokenService.createTokenByEmailAndId(email, userId, access)
        val refreshToken = tokenService.createTokenByEmailAndId(email, userId, refresh)

        response.apply {
            setHeader("Authorization", "Bearer $accessToken") // accessToken을 header에 저장
            jwtService.setJwtSessionCookie(refreshToken, this) // refreshToken을 cookie에 저장
        }

        log.info("remember me: 새로운 토큰 설정, accessToken: {}, refreshToken: {}", accessToken, refreshToken)
    }

    // 액세스 토큰 만료 시 자동으로 재발급
    private fun getNewAccessToken(request: HttpServletRequest): String {
        jwtService.getTokenByCookieName(request, REFRESH_TOKEN).let { refreshToken ->
            checkBlackList(refreshToken.toString())

            val claims = jwtService.parseAndValidateToken(refreshToken.get())
                ?: throw ServiceException(ErrorCode.USERINFO_NOT_FOUND)

            val (email, userId) = extractUserInfoFromClaims(claims)
            return tokenService.createTokenByEmailAndId(email, userId, access)
        }
    }

    private fun checkBlackList(token: String?) {
        // 블랙리스트 확인
        if (token.isNullOrBlank()) {
            log.info("검증할 토큰: {}", token)
            return
        }

        if (tokenService.isTokenBlacklisted(token.toString())) {
            throw ServiceException(ErrorCode.IN_BLACKLIST)
        }
    }

    private fun extractTokenFromHeader(request: HttpServletRequest): String? {
        return request.getHeader("Authorization")?.takeIf {
            it.startsWith("Bearer ") && it.length > 7 }
            ?.substring(7) // "Bearer " 이후의 토큰 부분만 반환
    }

    private fun handleJwt(claims: Claims) {
        val (email, userId) = extractUserInfoFromClaims(claims)
        setAuthentication(email, userId) // 인증 객체 설정
        log.info("인증 객체가 설정되었습니다. 사용자 ID: {}", userId)
    }

    private fun setAuthentication(email: String, userId: Long) {
        // 인증 객체에는 최소한의 정보만 담음 (id, 이메일, 권한)
        val customUserDetails = CustomUserDetails(userId, email)

        // 인증 완료 후 민감 데이터 제거를 위해 Credentials 필드를 null 처리
        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.authorities
        )

        SecurityContextHolder.getContext().authentication = authentication
        log.info("인증 성공: 사용자 ID={}, 이메일={}", userId, email)
    }

    // 필터링 제외 조건
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludedPaths = listOf(
            "/h2-console",
            "/login/oauth2/code/google",
            "/api/v2/users/"
        )
        val requestURI = request.requestURI

        return excludedPaths.any { requestURI.startsWith(it) } ||
                requestURI.matches(".*\\.(css|js|gif|png|jpg|ico)$".toRegex())
    }
}

