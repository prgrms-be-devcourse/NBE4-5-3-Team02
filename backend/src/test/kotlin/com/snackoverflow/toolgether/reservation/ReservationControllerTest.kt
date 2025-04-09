package com.snackoverflow.toolgether.reservation

import com.snackoverflow.toolgether.domain.reservation.controller.ReservationController
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("")
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

    lateinit var mockMvc: MockMvc

    @Autowired
    protected val context: WebApplicationContext? = null

    private val user1 = 1L
    private val user2 = 2L
    private val post1 = 1L

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    private fun requestReservation(): ResultActions {
        val jsonContent = """
        {
            "postId": ${post1},
            "renterId": ${user2},
            "ownerId": ${user1},
            "startTime": "${LocalDateTime.now().plusDays(1)}",
            "endTime": "${LocalDateTime.now().plusDays(3)}",
            "deposit": 10000.0,
            "rentalFee": 500.0
        }
        """.trimIndent()

        println("Sending JSON: $jsonContent")

        return mockMvc
            .perform(
                post("/api/v1/reservations/request")
                    .content(jsonContent)
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
    }

    @Test
    @Transactional
    @DisplayName("예약 요청 테스트")
    fun request(){
        val resultActions = requestReservation().andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("createReservation"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("예약 요청 성공"))
            .andExpect(jsonPath("$.data.status").value("REQUESTED"))
    }

    @Test
    @DisplayName("예약 승인 테스트")
    fun approve(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/approve")
        ).andDo(MockMvcResultHandlers.print())

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("approveReservation"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 승인 성공"))
    }

    @Test
    @Transactional
    @DisplayName("예약 거절 테스트")
    fun reject(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/reject")
        ).andDo(MockMvcResultHandlers.print())

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("rejectReservation"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 거절 성공"))
    }

    @Test
    @DisplayName("예약 취소 테스트")
    fun cancel(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/cancel")
        ).andDo(MockMvcResultHandlers.print())

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("cancelReservation"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 취소 성공"))
    }

    @Test
    @DisplayName("대여 시작 테스트")
    fun start(){
        val resultActions = requestReservation().andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("createReservation"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("예약 요청 성공"))
            .andExpect(jsonPath("$.data.status").value("REQUESTED"))

        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/2/start")
        ).andDo(MockMvcResultHandlers.print())

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("startRental"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("2번 예약 대여 시작 성공"))
    }

    @Test
    @DisplayName("소유자 이슈 테스트")
    fun issue1() {
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/2/ownerIssue")
                .param("reason", "DAMAGE_REPORTED")
        ).andDo(MockMvcResultHandlers.print())

        resultAction
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("ownerIssue"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("2번 예약 소유자에 의한 이슈로 환급 성공"))
    }

    @Test
    @DisplayName("대여자 이슈 테스트")
    fun issue2() {
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/2/renterIssue")
                .param("reason", "ITEM_LOSS")
        ).andDo(MockMvcResultHandlers.print())

        resultAction
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("renterIssue")) // 메서드 이름 수정
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("2번 예약 대여자에 의한 이슈로 환급 성공"))
    }

    @Test
    @DisplayName("예약 번호 조회 테스트")
    fun getReservation() {

        val resultAction = mockMvc.perform(get("/api/v1/reservations/1"))

        val mvcResult = resultAction.andReturn()
        val responseBody = mvcResult.response.contentAsString
        println("Response Body: $responseBody") // 응답 본문 출력

        resultAction
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("getReservationById")) // 메서드 이름 수정
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 조회 성공"))
    }

    @Test
    @DisplayName("게시글 번호 조회 테스트")
    fun getReservations() {
        val resultAction = mockMvc.perform(
            get("/api/v1/reservations/reservatedDates/1")
        )

        val mvcResult = resultAction.andReturn()
        val responseBody = mvcResult.response.contentAsString
        println("Response Body: $responseBody") // 응답 본문 출력

        resultAction
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("getReservedDates")) // 메서드 이름 수정
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 게시글의 예약 일정 조회 성공"))
    }
}