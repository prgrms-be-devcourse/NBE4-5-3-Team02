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
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.LoginUserArgumentResolver;
import com.snackoverflow.toolgether.global.util.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
	private PostImageService postImageService;

	@Mock
	private ReservationService reservationService;

	@Mock
	private UserRepository userRepository;

	// 로그인을 위해 추가
	@Mock
	private LoginUserArgumentResolver loginUserArgumentResolver;

	@Mock
	private S3Service s3Service;

	@InjectMocks
	private MypageController mypageController;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(mypageController)
				// 로그인을 위해 추가
				.setCustomArgumentResolvers(loginUserArgumentResolver)
				.build();
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
				.username("human123")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.phoneNumber("000-0000-0001")
				.address(address)
				.latitude(1.0)
				.longitude(1.0)
				.createdAt(LocalDateTime.now())
				.score(30)
				.credit(0)
				.build();

		MeInfoResponse meInfoResponse = MeInfoResponse.from(user);

		CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L); // ✅ username을 "human123"으로 설정

		when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
		when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);

		when(userService.findByUsername("human123")).thenReturn(user);
		when(userService.getUserForUsername("human123")).thenReturn(user);
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
	@DisplayName("예약 정보 조회 테스트 - 로그인 적용")
	public void testGetMyReservations() throws Exception {
		Address address = Address.builder()
				.mainAddress("서울시 강남구")
				.detailAddress("역삼동 123-45")
				.zipcode("12345")
				.build();

		User user = User.builder()
				.id(1L)
				.username("human123")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.phoneNumber("000-0000-0001")
				.address(address)
				.latitude(1.0)
				.longitude(1.0)
				.createdAt(LocalDateTime.now())
				.score(30)
				.credit(0)
				.build();

		User owner = User.builder()
				.id(2L)
				.username("owner123")
				.nickname("닉네임2")
				.email("owner@gmail.com")
				.phoneNumber("000-0000-0002")
				.address(Address.builder()
						.mainAddress("서울시 강남구")
						.detailAddress("역삼동 123-45")
						.zipcode("12345")
						.build())
				.createdAt(LocalDateTime.now())
				.score(40)
				.credit(10)
				.build();

		Post post = Post.builder()
				.id(10L)
				.title("Sample Post Title")
				.build();

		Reservation reservation = Reservation.builder()
				.id(2L)
				.post(post)
				.renter(user)
				.owner(owner)
				.createAt(LocalDateTime.now())
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusHours(1))
				.status(ReservationStatus.APPROVED)
				.amount(10000.0)
				.build();

		List<Reservation> rentals = new ArrayList<>();
		rentals.add(reservation);

		CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);

		when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
		when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);
		when(userService.findByUsername("human123")).thenReturn(user);
		when(reservationService.getRentalReservations(1L)).thenReturn(rentals);
		when(reservationService.getBorrowReservations(1L)).thenReturn(new ArrayList<>());
		when(reviewService.findByUserIdAndReservationId(anyLong(), anyLong())).thenReturn(Optional.empty());
		when(postImageService.getPostImagesByPostId(anyLong())).thenReturn(new ArrayList<>());

		ResultActions resultActions = mockMvc.perform(get("/api/v1/mypage/reservations"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("마이페이지 예약 정보 조회 성공"))
				.andExpect(jsonPath("$.data.rentals[0].title").value("Sample Post Title"))
				.andExpect(jsonPath("$.data.borrows").isEmpty());

		verify(userService, times(1)).findByUsername("human123");
		verify(reservationService, times(1)).getRentalReservations(1L);
		verify(reservationService, times(1)).getBorrowReservations(1L);
		verify(postImageService, times(1)).getPostImagesByPostId(anyLong());
	}



	@Test
	@DisplayName("내 정보 수정 - 로그인 적용")
	void testPatchMyInfo() throws Exception {
		Address address = Address.builder()
				.mainAddress("서울시 강남구")
				.detailAddress("역삼동 123-45")
				.zipcode("12345")
				.build();

		User user = User.builder()
				.id(1L)
				.username("human123")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.phoneNumber("000-0000-0001")
				.address(address)
				.latitude(1.0)
				.longitude(1.0)
				.createdAt(LocalDateTime.now())
				.score(30)
				.credit(0)
				.build();

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

		CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);

		when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
		when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);
		when(userService.findByUsername("human123")).thenReturn(user);
		when(userService.checkGeoInfo(any())).thenReturn(true);
		when(userService.checkMyInfoDuplicates(any(), any())).thenReturn("");

		ResultActions resultActions = mockMvc
				.perform(
						patch("/api/v1/mypage/me")
								.content(requestBody)
								.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
				)
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("내 정보 수정 성공"));

		verify(userService, times(1)).findByUsername("human123");
		verify(userService, times(1)).checkGeoInfo(any());
		verify(userService, times(1)).checkMyInfoDuplicates(any(), any());
		verify(userService, times(1)).updateMyInfo(any(), any());
	}


	@Test
	@DisplayName("회원 탈퇴 - 로그인 적용")
	void testDeleteMe() throws Exception {
		User user = User.builder()
				.id(1L)
				.username("human123")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.build();

		CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);

		when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
		when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);

		when(userService.findByUsername("human123")).thenReturn(user);

		ResultActions resultActions = mockMvc.perform(delete("/api/v1/mypage/me"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."));

		verify(userService, times(1)).findByUsername("human123");
		verify(userService, times(1)).deleteUser(user);
	}



	@Test
	@DisplayName("프로필 이미지 등록 - 로그인 적용")
	public void testPatchProfileImage() throws Exception {
		User user = User.builder()
				.id(1L)
				.username("human123")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.build();

		CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);

		when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
		when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);
		when(userService.findByUsername("human123")).thenReturn(user);

		MockMultipartFile profileImageFile = new MockMultipartFile(
				"profileImage",
				"testProfileImage.png",
				"image/png",
				"test profile image content".getBytes() // 파일 내용
		);

		ResultActions resultActions = mockMvc.perform(multipart("/api/v1/mypage/profile")
						.file(profileImageFile)
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.param("uuid", "testprofile")
				)
				.andDo(print());

		resultActions
				.andExpect(status().isOk());

		verify(userService, times(1)).findByUsername("human123");
		verify(userService, times(1)).postProfileImage(any(), any());
	}


	@Test
	@DisplayName("프로필 이미지 삭제 - 로그인 적용")
	public void testDeleteProfileImage() throws Exception {
		User user = User.builder()
				.id(1L)
				.username("human123")
				.nickname("닉네임1")
				.email("test1@gmail.com")
				.build();

		CustomUserDetails mockUserDetails = new CustomUserDetails("human123", "test1@gmail.com", 1L);

		when(loginUserArgumentResolver.supportsParameter(any())).thenReturn(true);
		when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUserDetails);
		when(userService.findByUsername("human123")).thenReturn(user);

		ResultActions resultActions = mockMvc.perform(delete("/api/v1/mypage/profile"))
				.andDo(print());

		resultActions
				.andExpect(status().isOk());

		verify(userService, times(1)).findByUsername("human123");
		verify(userService, times(1)).deleteProfileImage(user);
	}

}