package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.service.LocationService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import com.snackoverflow.toolgether.global.util.AESUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;

@Slf4j
@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class LocationController {

    /**
     * 바인딩 에러는 글로벌 오류 핸들러에서 일괄 처리: MethodArgumentNotValidException
     */
    private SecretKey secretKey;
    private final LocationService locationService;

    @PostConstruct
    private void init() {
        secretKey = AESUtil.createAESKey();
    }

    // 유저 위치 변경 change-location?lon= ... & lat= ...
    // 예시: 서울 성동구 -> 서울 용산구 (DB의 저장값이 바뀜)
    @PutMapping("/change-location")
    public RsData<?> changeLocation(@RequestParam double latitude,
                                    @RequestParam double longitude,
                                    @Login CustomUserDetails userDetails) {
        log.info("Change location 호출");
        String changeAddress = locationService.updateAddress(userDetails.getUserId(), latitude, longitude);
        return new RsData<>("200",
                "주소 정보가 수정되었습니다.",
                changeAddress);
    }

    // 유저 위치 받아오기 (회원만 가능) -> post 에 들어갔을 때 반경 설정을 위해서 정확한 위치 정보를 받아오는 것!
    // 세션에 저장하는 이유 -> 브라우저 종료 시 데이터 접근 불가 (다만 주기적 삭제가 필요)
    @PostMapping("/users/location")
    public RsData<?> saveLocation(@Login CustomUserDetails userDetails,
                                  @Validated @RequestBody LocationDto request,
                                  HttpSession session) throws Exception {

        // 민감 정보 암호화 후 저장
        String encryptedLatitude = AESUtil.encrypt(String.valueOf(request.latitude), secretKey);
        String encryptedLongitude = AESUtil.encrypt(String.valueOf(request.longitude), secretKey);

        session.setAttribute("latitude", encryptedLatitude);
        session.setAttribute("longitude", encryptedLongitude);
        session.setMaxInactiveInterval(60 * 60); // 1시간 유지

        log.info("세션에 저장된 위치 정보: userId: {}, {}, {}", userDetails.getUserId(), request.latitude, request.longitude);

        return new RsData<>("200",
                "위치 정보 저장 성공",
                "success");
    }

    private record LocationDto(double latitude, double longitude) {
    }

    // 위치 정보 조회
    @GetMapping("/users/location")
    public RsData<?> getLocation(HttpSession session) throws Exception {
        String encryptedLatitude = (String) session.getAttribute("latitude");
        String encryptedLongitude = (String) session.getAttribute("longitude");

        double latitude = Double.parseDouble(AESUtil.decrypt(encryptedLatitude, secretKey));
        double longitude = Double.parseDouble(AESUtil.decrypt(encryptedLongitude, secretKey));

        return new RsData<>("200",
                "조회 성공",
                new LocationDto(latitude, longitude));
    }
}
