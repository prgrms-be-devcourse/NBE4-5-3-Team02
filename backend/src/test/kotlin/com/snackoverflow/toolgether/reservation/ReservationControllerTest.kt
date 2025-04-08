package com.snackoverflow.toolgether.reservation

import com.snackoverflow.toolgether.domain.post.entity.Post
import com.snackoverflow.toolgether.domain.post.entity.enums.Category
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType
import com.snackoverflow.toolgether.domain.reservation.controller.ReservationController
import com.snackoverflow.toolgether.domain.reservation.entity.FailDue
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest
import com.snackoverflow.toolgether.domain.review.entity.Review
import com.snackoverflow.toolgether.domain.user.entity.Address
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.global.filter.LoginUserArgumentResolver
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("")
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Mock
    private val reservationService: ReservationService? = null

    // 로그인을 위해 추가
    @Mock
    private val loginUserArgumentResolver: LoginUserArgumentResolver? = null

    @InjectMocks
    private val reservationController : ReservationController? = null

    private var user1: User? = null
    private var user2: User? = null
    private var post: Post? = null
    private var reservation: Reservation? = null
    private var review: Review? = null
    private var reviewRequest: ReviewRequest? = null

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController) // 로그인을 위해 추가
            .setCustomArgumentResolvers(loginUserArgumentResolver)
            .build()

        user1 = User(
            1L,
            "human123",
            null,
            "test1@gmail.com",
            null,
            null,
            "000-0000-0001",
            "닉네임1",
            Address("서울시 강남구", "역삼동 123-45", "12345"),
            LocalDateTime.now(),
            null,
            null,
            true,
            null,
            30,
            0,
            null
        )

        user2 = User(
            2L,
            "seaman222",
            null,
            "test2@gmail.com",
            null,
            null,
            "000-0000-0002",
            "닉네임2",
            Address("서울시 강남구", "역삼동 123-45", "12345"),
            LocalDateTime.now(),
            null,
            null,
            true,
            null,
            30,
            0,
            null
        )

        post = Post(
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
            HashSet(),
            HashSet()
        )


        reservation = Reservation(
            1L,
            post!!,
            user1!!,
            user2!!,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            ReservationStatus.DONE,
            null.toString(),
            10000.0
        )

        review = Review(
            1L,
            user1!!,
            user2!!,
            reservation!!,
            5,
            5,
            5,
            LocalDateTime.now()
        )

        reviewRequest = ReviewRequest(
            Companion.reservationId,
            4,
            5,
            4
        )
    }

    private fun requestReservation(): ResultActions {
        val jsonContent = """
        {
            "postId": ${post?.id},
            "renterId": ${user2?.id},
            "ownerId": ${user1?.id},
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
    @DisplayName("예약 요청 테스트")
    fun request(){
        val resultActions = requestReservation()

        resultActions
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("createReservation"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("예약 요청 성공"))
    }

    @Test
    @DisplayName("예약 승인 테스트")
    fun approve(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/approve")
        )

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("approveReservation"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 승인 성공"))
    }

    @Test
    @DisplayName("예약 거절 테스트")
    fun reject(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/reject")
        )

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
        )

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("cancelReservation"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 취소 성공"))
    }

    @Test
    @DisplayName("대여 시작 테스트")
    fun start(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/start")
        )

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("startRental"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 대여 시작 성공"))
    }

    @Test
    @DisplayName("대여 종료 테스트")
    fun complete(){
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/complete")
        )

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("completeRental"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 대여 종료 성공"))
    }

    @Test
    @DisplayName("소유자 이슈 테스트")
    fun issue1() {
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/ownerIssue")
                .param("reason", "소유자 사정으로 인한 취소")
        )

        resultAction
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("ownerIssue"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 소유자에 의한 이슈로 환급 성공"))
    }

    @Test
    @DisplayName("대여자 이슈 테스트")
    fun issue2() {
        val resultAction = mockMvc.perform(
            patch("/api/v1/reservations/1/renterIssue")
                .param("reason", "대여자 사정으로 인한 취소")
        )

        resultAction
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(ReservationController::class.java))
            .andExpect(handler().methodName("renterIssue")) // 메서드 이름 수정
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약 대여자에 의한 이슈로 환급 성공"))
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


    companion object {
        private const val reservationId = 1L
    }
}