package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import com.snackoverflow.toolgether.domain.postimage.service.PostImageService;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.dto.MeInfoResponse;
import com.snackoverflow.toolgether.domain.user.dto.MyReservationInfoResponse;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final ReservationService reservationService;
    private final PostImageService postImageService;

    @GetMapping("/me")
    public RsData<MeInfoResponse> getMyInfo(
            @Login CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        MeInfoResponse meInfoResponse = userService.getMeInfo(userId);

        return new RsData<>(
                "200-1",
                "내 정보 조회 성공",
                meInfoResponse
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/reservations")
    public RsData<Map<String, List<MyReservationInfoResponse>>> getMyReservations(
            @Login CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();
        User user = userService.findByUsername(username);
        Long userId = user.getId();

        List<Reservation> rentals = reservationService.getRentalReservations(userId);
        List<Reservation> borrows = reservationService.getBorrowReservations(userId);

        List<MyReservationInfoResponse> rentalResponses = rentals.stream()
                .map(reservation -> {
                    String imageUrl = null;
                    List<PostImage> images = postImageService.getPostImagesByPostId(reservation.getPost().getId());  //PostImageService 호출
                    if (images != null && !images.isEmpty()) {
                        imageUrl = images.get(0).getPostImage();
                    }
                    boolean isReviewed = reviewService.findByUserIdAndReservationId(userId, reservation.getId()).isPresent();

                    return MyReservationInfoResponse.from(reservation, imageUrl, isReviewed);
                })
                .collect(Collectors.toList());

        List<MyReservationInfoResponse> borrowResponses = borrows.stream()
                .map(reservation -> {
                    String imageUrl = null;
                    List<PostImage> images = postImageService.getPostImagesByPostId(reservation.getPost().getId());  //PostImageService 호출
                    if (images != null && !images.isEmpty()) {
                        imageUrl = images.get(0).getPostImage();
                    }
                    boolean isReviewed = reviewService.findByUserIdAndReservationId(userId, reservation.getId()).isPresent();
                    return MyReservationInfoResponse.from(reservation, imageUrl, isReviewed);
                })
                .collect(Collectors.toList());

        Map<String, List<MyReservationInfoResponse>> data = new HashMap<>();
        data.put("rentals", rentalResponses);
        data.put("borrows", borrowResponses);

        return new RsData<>(
                "200-1",
                "마이페이지 예약 정보 조회 성공",
                data
        );
    }
}
