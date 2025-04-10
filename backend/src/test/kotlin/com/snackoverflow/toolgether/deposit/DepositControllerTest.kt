package com.snackoverflow.toolgether.deposit

import com.snackoverflow.toolgether.domain.deposit.controller.DepositController
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@ActiveProfiles("")
@AutoConfigureMockMvc
@Transactional
class DepositControllerTest {
    lateinit var mockMvc: MockMvc

    @Autowired
    protected val context: WebApplicationContext? = null

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @DisplayName("보증금 내역 조회")
    fun deposits() {
        val resultAction = mockMvc.perform(
            get("/api/v1/deposits/rid/1")
        ).andDo(MockMvcResultHandlers.print())

        resultAction.andExpect(status().isOk)
            .andExpect(handler().handlerType(DepositController::class.java))
            .andExpect(handler().methodName("findDepositHistoryByReservationId"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("1번 예약의 보증금 내역이 조회되었습니다."))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
    }
}