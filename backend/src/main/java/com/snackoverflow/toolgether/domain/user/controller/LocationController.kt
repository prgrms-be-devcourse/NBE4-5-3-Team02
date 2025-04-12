package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.service.LocationService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login;
import com.snackoverflow.toolgether.global.util.AESUtil
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;

import java.util.Map;
import kotlin.apply
import kotlin.jvm.java
import kotlin.let
import kotlin.text.toDouble
import kotlin.to

@RestController
@RequestMapping("/api/v2")
class LocationController(
    private val locationService: LocationService
) {

    /**
     * 바인딩 에러는 글로벌 오류 핸들러에서 일괄 처리: MethodArgumentNotValidException
     */
    private val log = LoggerFactory.getLogger(LocationController::class.java)
    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        secretKey = AESUtil.createAESKey()
    }

    // 유저 위치 변경 change-location?lon= ... & lat= ...
    // 예시: 서울 성동구 -> 서울 용산구 (DB의 저장값이 바뀜)
    @PutMapping("/change-location")
    fun updateLocation(
        @RequestParam latitude: Double, @RequestParam longitude: Double,
        @Login userDetails: CustomUserDetails
    ): RsData<Any> {

        log.info("Change location 호출");
        locationService.updateAddress(userDetails.userId, latitude, longitude);

        return RsData("200", "주소 정보가 수정되었습니다.")
    }

    /**
     * 유저 위치 받아오기 -> post 에 들어갔을 때 반경 설정을 위해서 정확한 위치 정보를 받아오는 것!
     * 세션 아이디를 기준으로 생성 (비회원도 등록 가능)
     * 세션에 저장하는 이유 -> 브라우저 종료 시 데이터 접근 불가 (다만 주기적 삭제가 필요)
     */

    data class LocationInfo(val latitude: Double, val longitude: Double)

    @PostMapping("/users/location")
    fun saveLocation(
        @Validated @RequestBody locationInfo: LocationInfo,
        request: HttpServletRequest, session: HttpSession
    ): RsData<Any> {

        val sessionId = request.session.id

        // 민감 정보 암호화 후 저장
        val sessionData = mapOf(
            "latitude" to AESUtil.encrypt(locationInfo.latitude.toString(), secretKey),
            "longitude" to AESUtil.encrypt(locationInfo.longitude.toString(), secretKey)
        )

        session.apply {
            setAttribute("sessionId", sessionData) // 세션 아이디를 기준으로 위도, 경도를 저장함
            maxInactiveInterval = 60 * 60 // 1시간 유지
        }

        log.info("세션에 저장된 위치 정보: sessionId: {}, {}, {}", sessionId, locationInfo.latitude, locationInfo.longitude)

        return RsData<Any>("200", "위치 정보 저장 성공", "success")
    }

    // 위치 정보 조회
    @GetMapping("/users/location")
    fun getLocation(request: HttpServletRequest, session: HttpSession): RsData<LocationInfo> {
        val sessionData = session.getAttribute(request.session.id) as? Map<String, String>
            ?: return RsData("404", "위치 정보가 존재하지 않습니다.", null)

        val latitude = sessionData["latitude"]?.let { AESUtil.decrypt(it, secretKey).toDouble() }
            ?: throw IllegalStateException("위도 정보 복호화 실패")
        val longitude = sessionData["longitude"]?.let { AESUtil.decrypt(it, secretKey).toDouble() }
            ?: throw IllegalStateException("경도 정보 복호화 실패")

        return RsData("200", "조회 성공", LocationInfo(latitude, longitude))
    }
}
