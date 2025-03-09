package com.snackoverflow.toolgether;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.postimage.service.PostImageService;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.controller.MypageController;
import com.snackoverflow.toolgether.domain.user.dto.MeInfoResponse;
import com.snackoverflow.toolgether.domain.user.dto.MyReservationInfoResponse;
import com.snackoverflow.toolgether.domain.user.dto.request.PatchMyInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.dto.RsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class MypageControllerTest {

	private MockMvc mockMvc;

	@Mock
	private UserService userService;

	@Mock
	private ReviewService reviewService;

	@Mock
	private ReservationService reservationService;

	@Mock
	private PostImageService postImageService;

	@InjectMocks
	private MypageController mypageController;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(mypageController).build();
	}

	@Test
	@DisplayName("내 정보 조회 테스트")
	public void testGetMyInfo() throws Exception {
		Address address = Address.builder()
				.mainAddress("서울시 강남구")
				.detailAddress("역삼동 123-45")
				.zipcode("12345")
				.build();

		User user = User.builder()
				.id(1L)
				.username("testId1")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.phoneNumber("000-0000-0001")
				.address(address)
				.createdAt(LocalDateTime.now())
				.score(30)
				.credit(0)
				.build();

		MeInfoResponse meInfoResponse = MeInfoResponse.from(user);
		when(userService.getMeInfo(1L)).thenReturn(meInfoResponse);

		ResultActions resultActions = mockMvc.perform(get("/api/v1/mypage/me"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("내 정보 조회 성공"))
				.andExpect(jsonPath("$.data.nickname").value("닉네임1"))
				.andExpect(jsonPath("$.data.email").value("test1@gmail.com"));

		verify(userService, times(1)).getMeInfo(1L);
	}

	@Test
	@DisplayName("예약 정보 조회 테스트")
	public void testGetMyReservations() throws Exception {
		User user1 = User.builder()
				.id(1L)
				.username("testId1")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.phoneNumber("000-0000-0001")
				.address( Address.builder()
						.mainAddress("서울시 강남구")
						.detailAddress("역삼동 123-45")
						.zipcode("12345")
						.build())
				.createdAt(LocalDateTime.now())
				.score(30)
				.credit(0)
				.build();

		User user2 = User.builder()
				.id(1L)
				.username("testId1")
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

		Post post = Post.builder()
				.id(10L)
				.title("Sample Post Title")
				.build();
		Reservation reservation = Reservation.builder()
				.id(2L)
				.post(post)
				.renter(user1)
				.owner(user2)
				.createAt(LocalDateTime.now())
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusHours(1))
				.status(ReservationStatus.APPROVED)
				.amount(10000.0)
				.build();

		List<Reservation> rentals = new ArrayList<>();
		rentals.add(reservation);

		when(reservationService.getRentalReservations(1L)).thenReturn(rentals);
		when(reservationService.getBorrowReservations(1L)).thenReturn(new ArrayList<>());

		when(reviewService.findByUserIdAndReservationId(anyLong(), anyLong())).thenReturn(Optional.empty());

		ResultActions resultActions = mockMvc.perform(get("/api/v1/mypage/reservations"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("마이페이지 예약 정보 조회 성공"))
				.andExpect(jsonPath("$.data.rentals[0].title").value("Sample Post Title"))
				.andExpect(jsonPath("$.data.borrows").isEmpty());

		verify(reservationService, times(1)).getRentalReservations(1L);
		verify(reservationService, times(1)).getBorrowReservations(2L);
	}

	@Test
	@DisplayName("내 정보 수정")
	void testPatchMyInfo() throws Exception {

		String modifiedEmail = "modifiedMail123@gmail.com";
		String modifiedNickname = "수정된닉네임";
		String modifiedPhoneNumber = "01012345678";
		String mainAddress = "수정시 수정구";
		String detailAddress = "수정동 123-45";
		String zipcode = "12345";
		String longitude = "122.2222";
		String latitude = "44.4444";

		String requestBody = """
                {
                    "email": "%s",
                    "phoneNumber": "%s",
                    "nickname": "%s",
                    "address": {
                        "mainAddress": "%s",
                        "detailAddress": "%s",
                        "zipcode": "%s"
                    },
                    "longitude": "%s",
                    "latitude": "%s"
                }
                """.formatted(
				modifiedEmail,
				modifiedPhoneNumber,
				modifiedNickname,
				mainAddress,
				detailAddress,
				zipcode,
				longitude,
				latitude
		).stripIndent();

		ResultActions resultActions = mockMvc
				.perform(
						patch("/api/v1/mypage/me")
								.content(requestBody)
								.contentType(
										new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
								)
				)
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("내 정보 수정 성공"));

		verify(userService, times(1)).updateMyInfo(any(), any());

	}

	@Test
	@DisplayName("회원 탈퇴")
	void testDeleteMe() throws Exception {

		ResultActions resultActions = mockMvc.perform(delete("/api/v1/mypage/me"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk());

		verify(userService, times(1)).deleteUser(any());
	}

	@Test
	@DisplayName("프로필 이미지 등록")
	public void testPatchProfileImage() throws Exception {
		String requestBody = """
                {
                    "uuid": "%s"
                }
                """.formatted(
				"testprofile"
		).stripIndent();
		ResultActions resultActions = mockMvc.perform(post("/api/v1/mypage/profile")
				.content(requestBody)
				.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
				))
				.andDo(print());

		resultActions
				.andExpect(status().isOk());

		verify(userService, times(1)).postProfileImage(any(), any());
	}

	@Test
	@DisplayName("프로필 이미지 삭제")
	public void testDeleteProfileImage() throws Exception {
		ResultActions resultActions = mockMvc.perform(delete("/api/v1/mypage/profile"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk());

		verify(userService, times(1)).deleteProfileImage(any());
	}
}