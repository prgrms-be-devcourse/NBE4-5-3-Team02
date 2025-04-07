package com.snackoverflow.toolgether.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest;
import com.snackoverflow.toolgether.domain.review.entity.Review;
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
import java.util.HashSet;
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

    private User user1;
    private User user2;
    private Post post;
    private Reservation reservation;
    private Review review;
    private ReviewRequest reviewRequest;
    private Long reservationId = 1L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                // 로그인을 위해 추가
                .setCustomArgumentResolvers(loginUserArgumentResolver)
                .build();

        user1 = new User(
                1L,
                "human123",
                null,
                "test1@gmail.com",
                null,
                null,
                "000-0000-0001",
                "닉네임1",
                new Address("서울시 강남구", "역삼동 123-45", "12345"),
                LocalDateTime.now(),
                null,
                null,
                true,
                null,
                30,
                0,
                null
        );

        user2 = new User(
                2L,
                "seaman222",
                null,
                "test2@gmail.com",
                null,
                null,
                "000-0000-0002",
                "닉네임2",
                new Address("서울시 강남구", "역삼동 123-45", "12345"),
                LocalDateTime.now(),
                null,
                null,
                true,
                null,
                30,
                0,
                null
        );

        post = new Post(
                10L,
                user1,
                "Sample Post Title",
                "Sample Content",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Category.TOOL,
                PriceType.DAY,
                10000,
                37.5,
                127.0,
                0,
                new HashSet<PostImage>(),
                new HashSet<PostAvailability>()
        );


        reservation = new Reservation(
                1L,
                post,
                user1,
                user2,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                ReservationStatus.DONE,
                null,
                10000.0
        );

        review = new Review(
                1L,
                user1,
                user2,
                reservation,
                5,
                5,
                5,
                LocalDateTime.now());

        reviewRequest = new ReviewRequest(
                reservationId,
                4,
                5,
                4
        );
    }

    @Test
    @DisplayName("리뷰 작성 테스트 - @Login 적용")
    public void testPostReviewWithLogin() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String reviewRequestJson = objectMapper.writeValueAsString(reviewRequest);

        // 로그인을 위해 추가
        CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);
        when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);

        when(reservationService.getReservationByIdForReview(reservationId)).thenReturn(Optional.of(reservation));

        when(reviewService.findByUserIdAndReservationId(reservationId, user1.getId())).thenReturn(Optional.empty());

        when(userService.findUserById(1L)).thenReturn(user1);
        when(userService.getUserForUsername("human123")).thenReturn(user1);

        doNothing().when(reviewService).create(any(ReviewRequest.class), any(Reservation.class), any(User.class));

        ResultActions resultActions = mockMvc.perform(post("/api/v1/review/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(reviewRequestJson)
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("리뷰가 완료되었습니다"));

        verify(reviewService, times(1)).create(any(ReviewRequest.class), any(Reservation.class), any(User.class));
    }

    @Test
    @DisplayName("리뷰 작성 실패 테스트 - 이미 리뷰를 작성한 경우")
    public void testPostReviewAlreadyExists() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String reviewRequestJson = objectMapper.writeValueAsString(reviewRequest);

        // 로그인을 위해 추가
        CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);
        when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);

        when(reservationService.getReservationByIdForReview(reservationId)).thenReturn(Optional.of(reservation));

        // 컨트롤러에서 사용하는 existsUserIdAndReservationId 메서드를 Mocking
        when(reviewService.existsUserIdAndReservationId(user1.getId(), reservation.getId())).thenReturn(true);

        when(userService.findUserById(1L)).thenReturn(user1);
        when(userService.getUserForUsername("human123")).thenReturn(user1);

        doNothing().when(reviewService).create(any(ReviewRequest.class), any(Reservation.class), any(User.class));

        ResultActions resultActions = mockMvc.perform(post("/api/v1/review/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(reviewRequestJson)
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$.code").value("409-1")) // 예시 에러 코드
                .andExpect(jsonPath("$.msg").value("이미 작성한 리뷰가 존재합니다")); // 예시 에러 메시지

        verify(reviewService, times(0)).create(any(ReviewRequest.class), any(Reservation.class), any(User.class));
    }
}