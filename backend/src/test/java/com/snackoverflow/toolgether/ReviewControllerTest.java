package com.snackoverflow.toolgether;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.review.controller.ReviewController;
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.LoginUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ReservationService reservationService;

    // 로그인을 위해 추가
    @Mock
    private LoginUserArgumentResolver loginUserArgumentResolver;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                // 로그인을 위해 추가
                .setCustomArgumentResolvers(loginUserArgumentResolver)
                .build();
    }

    @Test
    @DisplayName("리뷰 작성 테스트 - @Login 적용")
    public void testPostReviewWithLogin() throws Exception {
        Long reservationId = 1L;

        User user1 = User.builder()
                .username("human123")
                .nickname("닉네임1")
                .email("test1@gmail.com")
                .phoneNumber("000-0000-0001")
                .address(Address.builder()
                        .mainAddress("서울시 강남구")
                        .detailAddress("역삼동 123-45")
                        .zipcode("12345")
                        .build())
                .createdAt(LocalDateTime.now())
                .score(30)
                .credit(0)
                .build();

        User user2 = User.builder()
                .username("seaman222")
                .nickname("닉네임2")
                .email("test2@gmail.com")
                .phoneNumber("000-0000-0002")
                .address(Address.builder()
                        .mainAddress("서울시 강남구")
                        .detailAddress("역삼동 123-45")
                        .zipcode("12345")
                        .build())
                .createdAt(LocalDateTime.now())
                .score(30)
                .credit(0)
                .build();

        Post post = Post.builder()
                .id(10L)
                .title("Sample Post Title")
                .build();

        Reservation reservation = Reservation.builder()
                .post(post)
                .renter(user1)
                .owner(user2)
                .createAt(LocalDateTime.now())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .status(ReservationStatus.DONE)
                .amount(10000.0)
                .build();

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReservationId(reservationId);
        reviewRequest.setProductScore(4);
        reviewRequest.setTimeScore(5);
        reviewRequest.setKindnessScore(4);

        ObjectMapper objectMapper = new ObjectMapper();
        String reviewRequestJson = objectMapper.writeValueAsString(reviewRequest);

        // 로그인을 위해 추가
        CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);
        when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);

        when(reservationService.getReservationByIdForReview(reservationId)).thenReturn(Optional.of(reservation));

        when(reviewService.findByUserIdAndReservationId(reservationId, user1.getId())).thenReturn(Optional.empty());

        when(userService.getUserForUsername("human123")).thenReturn(user1);

        doNothing().when(reviewService).create(any(ReviewRequest.class), any(Reservation.class), any(User.class));

        ResultActions resultActions = mockMvc.perform(post("/api/v1/review/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(reviewRequestJson)
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("리뷰가 완료되었습니다"));

        verify(reviewService, times(1)).create(any(ReviewRequest.class), any(Reservation.class), any(User.class));
    }
}
