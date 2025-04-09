package com.snackoverflow.toolgether.domain.user.controller

import com.snackoverflow.toolgether.domain.postimage.service.PostImageService
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService
import com.snackoverflow.toolgether.domain.review.service.ReviewService
import com.snackoverflow.toolgether.domain.user.dto.MeInfoResponse
import com.snackoverflow.toolgether.domain.user.dto.MyReservationInfoResponse
import com.snackoverflow.toolgether.domain.user.dto.request.PatchMyInfoRequest
import com.snackoverflow.toolgether.domain.user.service.UserService
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login
import lombok.RequiredArgsConstructor
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
class MypageController(
    private val userService: UserService,
    private val reviewService: ReviewService,
    private val reservationService: ReservationService,
    private val postImageService: PostImageService,
) {
    //내 정보 조회
    @GetMapping("/me")
    fun getMyInfo(
        @Login customUserDetails: CustomUserDetails
    ): RsData<MeInfoResponse> {
        val userId = customUserDetails.userId
        val meInfoResponse = userService.getMeInfo(userId)

        return RsData(
            "200-1",
            "내 정보 조회 성공",
            meInfoResponse
        )
    }

    //예약 조회
    @Transactional(readOnly = true)
    @GetMapping("/reservations")
    fun getMyReservations(
        @Login customUserDetails: CustomUserDetails
    ): RsData<Map<String, List<MyReservationInfoResponse>>> {
        val userId = customUserDetails.userId

        val rentals = reservationService.getRentalReservations(userId)
        val borrows = reservationService.getBorrowReservations(userId)

        val rentalResponses = rentals.map { reservation ->
                val imageUrl: String? = postImageService.getPostImagesByPostId(reservation.post.id)
                    ?.takeIf { it.isNotEmpty() }
                    ?.get(0)
                    ?.getImageUrl()
                val isReviewed = reviewService.findByUserIdAndReservationId(userId, reservation.id!!).isPresent
                MyReservationInfoResponse.from(reservation, imageUrl, isReviewed)
            }

        val borrowResponses = borrows.stream()
            .map { reservation: Reservation ->
                var imageUrl: String? = null
                val images = postImageService.getPostImagesByPostId(reservation.post.id) //PostImageService 호출
                if (images != null && images.isNotEmpty()) {
                    imageUrl = images.get(0).getImageUrl();
                }
                val isReviewed = reviewService.findByUserIdAndReservationId(userId, reservation.id!!).isPresent
                MyReservationInfoResponse.from(reservation, imageUrl, isReviewed)
            }
            .collect(Collectors.toList())

        val data: MutableMap<String, List<MyReservationInfoResponse>> = HashMap()
        data["rentals"] = rentalResponses
        data["borrows"] = borrowResponses

        return RsData(
            "200-1",
            "마이페이지 예약 정보 조회 성공",
            data
        )
    }

    //프로필 이미지 업로드
    @PostMapping("/profile")
    fun postProfileimage(
        @Login customUserDetails: CustomUserDetails,
        @RequestParam("profileImage") profileImage: MultipartFile?
    ): RsData<Void> {
        val userId = customUserDetails.userId
        val user = userService.findUserById(userId)
        userService.postProfileImage(user, profileImage)
        return RsData(
            "200-1",
            "프로필 이미지 업로드가 완료되었습니다"
        )
    }

    //프로필 이미지 삭제
    @DeleteMapping("/profile")
    fun deleteProfileImage(
        @Login customUserDetails: CustomUserDetails
    ): RsData<Void> {
        val userId = customUserDetails.userId
        val user = userService.findUserById(userId)
        userService.deleteProfileImage(user)
        return RsData(
            "200-1",
            "프로필 이미지 삭제가 완료되었습니다"
        )
    }

    //이미지 제외한 수정 가능한 내 정보 수정
    @PatchMapping("/me")
    fun PostMyInfo(
        @Login customUserDetails: CustomUserDetails,
        @RequestBody @Validated request: PatchMyInfoRequest
    ): RsData<Void> {
        val userId = customUserDetails.userId
        val user = userService.findUserById(userId)
        val isGeoInfoValid = userService.checkGeoInfo(request)
        if (!isGeoInfoValid) {
            return RsData(
                "400-1",
                "위치 검증 실패"
            )
        }
        val duplicateCheck = userService.checkMyInfoDuplicates(user, request)
        if (duplicateCheck == "닉네임" || duplicateCheck == "전화번호") {
            return RsData(
                "409-1",
                "%s 중복".formatted(duplicateCheck)
            )
        }
        userService.updateMyInfo(user, request)

        return RsData(
            "200-1",
            "내 정보 수정 성공"
        )
    }

    //회원 탈퇴
    @DeleteMapping("/me")
    fun DeleteMe(
        @Login customUserDetails: CustomUserDetails
    ): RsData<Void> {
        val userId = customUserDetails.userId
        val user = userService.findUserById(userId)
        userService.deleteUser(user)
        return RsData(
            "200-1",
            "회원 탈퇴가 완료되었습니다."
        )
    }
}
