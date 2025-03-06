package com.snackoverflow.toolgether;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.postimage.service.PostImageService;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.controller.MypageController;
import com.snackoverflow.toolgether.domain.user.dto.AddressInfo;
import com.snackoverflow.toolgether.domain.user.dto.MeInfoResponse;
import com.snackoverflow.toolgether.domain.user.dto.MyReservationInfoResponse;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.dto.RsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
				.address(Address.builder()
						.mainAddress("서울시 강남구")
						.detailAddress("역삼동 123-45")
						.zipcode("12345")
						.build())
				.createdAt(LocalDateTime.now())
				.score(30)
				.credit(0)
				.build();
		Post post1 = Post.builder()
				.id(10L)
				.title("Sample Post Title")
				.build();
		Reservation reservation1 = Reservation.builder()
				.id(1L)
				.post(post1)
				.renter(user1)
				.owner(user1)
				.createAt(LocalDateTime.now())
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusHours(1))
				.status(ReservationStatus.APPROVED)
				.amount(10000.0)
				.build();

		MyReservationInfoResponse myReservationInfoResponse = MyReservationInfoResponse.from(reservation1, "imageUrl", false);

		List<Reservation> rentals = new ArrayList<>();
		rentals.add(reservation1);

		List<MyReservationInfoResponse> rentalResponses = new ArrayList<>();
		rentalResponses.add(myReservationInfoResponse);

		Map<String, List<MyReservationInfoResponse>> data = new HashMap<>();
		data.put("rentals", rentalResponses);
		data.put("borrows", new ArrayList<>());

		RsData<Map<String, List<MyReservationInfoResponse>>> rsData = new RsData<>("200-1", "마이페이지 예약 정보 조회 성공", data);


		when(reservationService.getRentalReservations(2L)).thenReturn(rentals);
		when(reservationService.getBorrowReservations(2L)).thenReturn(new ArrayList<>());
		when(postImageService.getPostImagesByPostId(any())).thenReturn(new ArrayList<>());
		when(reviewService.findByUserIdAndReservationId(anyLong(), anyLong())).thenReturn(Optional.empty());

		ResultActions resultActions = mockMvc.perform(get("/api/v1/mypage/reservations"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("마이페이지 예약 정보 조회 성공"))
				.andExpect(jsonPath("$.data.rentals[0].title").value("Sample Post Title"))
				.andExpect(jsonPath("$.data.borrows").isEmpty());

		verify(reservationService, times(1)).getRentalReservations(2L);
		verify(reservationService, times(1)).getBorrowReservations(2L);
	}
}