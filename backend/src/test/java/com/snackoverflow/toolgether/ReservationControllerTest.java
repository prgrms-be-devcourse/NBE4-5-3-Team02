package com.snackoverflow.toolgether;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.snackoverflow.toolgether.domain.reservation.controller.ReservationController;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse;
import com.snackoverflow.toolgether.domain.reservation.entity.FailDue;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;

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

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReservationControllerTest {

	private MockMvc mockMvc;

	@Mock
	private ReservationService reservationService;

	@InjectMocks
	private ReservationController reservationController;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
	}

	@Test
	@DisplayName("예약 요청 테스트")
	public void testRequestReservation() throws Exception {
		ReservationRequest request = new ReservationRequest(1L, 2L, 3L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 1000.0, 500.0);
		ReservationResponse response = new ReservationResponse(1L, "REQUESTED",  1L, LocalDateTime.now(),  LocalDateTime.now(), 20000.0, "", 3L, 2L);

		when(reservationService.requestReservation(any(ReservationRequest.class))).thenReturn(response);

		ResultActions resultActions = mockMvc.perform(post("/api/v1/reservations/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).requestReservation(any(ReservationRequest.class));
	}

	@Test
	@DisplayName("예약 승인 테스트")
	public void testApproveReservation() throws Exception {
		ResultActions resultActions = mockMvc.perform(post("/api/v1/reservations/1/approve"))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).approveReservation(1L);
	}

	@Test
	@DisplayName("예약 거절 테스트")
	public void testRejectReservation() throws Exception {
		ResultActions resultActions = mockMvc.perform(patch("/api/v1/reservations/1/reject")
				.param("reason", "Reason for rejection"))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).rejectReservation(1L, "Reason for rejection");
	}

	@Test
	@DisplayName("대여 시작 변경 테스트")
	public void testStartRental() throws Exception {
		ResultActions resultActions = mockMvc.perform(patch("/api/v1/reservations/1/start"))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).startRental(1L);
	}

	@Test
	@DisplayName("대여 종료 변경 테스트")
	public void testCompleteRental() throws Exception {
		ResultActions resultActions = mockMvc.perform(patch("/api/v1/reservations/1/complete"))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).completeRental(1L);
	}

	@Test
	@DisplayName("소유자 이슈로 인한 환불 테스트")
	public void testOwnerIssue() throws Exception {
		ResultActions resultActions = mockMvc.perform(patch("/api/v1/reservations/1/ownerIssue")
				.param("reason", "Owner issue reason"))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).failDueTo(1L, "Owner issue reason", FailDue.OWNER_ISSUE);
	}

	@Test
	@DisplayName("대여자 이슈로 인한 환불 테스트")
	public void testRenterIssue() throws Exception {
		ResultActions resultActions = mockMvc.perform(patch("/api/v1/reservations/1/renterIssue")
				.param("reason", "Renter issue reason"))
			.andDo(print());

		resultActions
			.andExpect(status().isOk());

		verify(reservationService, times(1)).failDueTo(1L, "Renter issue reason", FailDue.RENTER_ISSUE);
	}
}
