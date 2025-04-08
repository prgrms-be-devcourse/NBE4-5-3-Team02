package com.snackoverflow.toolgether.domain.review.scheduler;

import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.scheduler.lock.entity.SchedulerLock;
import com.snackoverflow.toolgether.domain.review.scheduler.lock.repository.SchedulerLockRepository;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.util.ScheduleUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ReviewScoreSchedulerTest {

    @Autowired
    private ReviewScoreScheduler reviewScoreScheduler;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SchedulerLockRepository schedulerLockRepository;

    private User mockUser;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        // 사용자 모킹
        mockUser = Mockito.mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        // workingTest와 decayTest에서 다른 상황을 만들기 위해 초기 점수 설정은 테스트별로 stub 할 수 있음

        // 리뷰 모킹 (평균: (4+5+3)/3 = 4.0 → determineScoreChange(4.0)= +1.0)
        mockReview = Mockito.mock(Review.class);
        when(mockReview.getReviewee()).thenReturn(mockUser);
        // 최근 리뷰로 가정: 1달 이내 → 가중치 1.0
        when(mockReview.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(10));
        when(mockReview.getProductScore()).thenReturn(4);
        when(mockReview.getTimeScore()).thenReturn(5);
        when(mockReview.getKindnessScore()).thenReturn(3);
    }

    @Test
    @DisplayName("새 리뷰 집계 로직 정상 작동 테스트")
    void workingTest() {
        // 락 관련 stub
        when(schedulerLockRepository.findByLockNameWithLock(anyString())).thenReturn(Optional.empty());
        // 새 리뷰: 지난 1주일 내에 작성된 리뷰가 1건 존재
        when(reviewService.getReviewsCreatedAfter(any())).thenReturn(List.of(mockReview));
        // inactive 사용자: 최근 6개월 리뷰가 있는 사용자 목록은 빈 리스트로 설정 → 감쇠 로직 미실행
        when(userService.getUsersWithoutReviewsSince(any())).thenReturn(List.of());

        // 사용자의 초기 점수를 50으로 stub (긍정 리뷰: determineScoreChange(평균 4.0) → +1.0)
        when(mockUser.getScore()).thenReturn(50);

        reviewScoreScheduler.aggregateReviewScores();

        // 집계 과정에서, 새 리뷰 집계에 의해 userService.updateUserScore()가 호출되어야 함.
        // 이 경우, 50 + 1 = 51이 업데이트되어야 함.
        verify(userService, times(1)).updateUserScore(eq(1L), eq(51.0));

        // 락 획득, 저장, 삭제가 1번씩 호출되었는지 확인
        verify(schedulerLockRepository, times(1)).findByLockNameWithLock(anyString());
        verify(schedulerLockRepository, times(1)).save(any());
        verify(schedulerLockRepository, times(1)).deleteById(anyString());
    }

    @Test
    @DisplayName("락 획득 실패 테스트")
    void lockFailureTest() {
        when(schedulerLockRepository.findByLockNameWithLock(anyString()))
                .thenReturn(Optional.of(new SchedulerLock()));

        reviewScoreScheduler.aggregateReviewScores();

        verify(schedulerLockRepository, times(1)).findByLockNameWithLock(anyString());
        verify(schedulerLockRepository, never()).save(any());
        verify(schedulerLockRepository, never()).deleteById(anyString());
        verify(reviewService, never()).getReviewsCreatedAfter(any());
        verify(userService, never()).updateUserScore(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("감쇠 로직 테스트: 새 리뷰가 없을 때 inactive 사용자 점수 감소")
    void decayTest() {
        // 새 리뷰가 없는 경우를 시뮬레이션: 지난 1주일 내 새 리뷰 없음
        when(schedulerLockRepository.findByLockNameWithLock(anyString())).thenReturn(Optional.empty());
        when(reviewService.getReviewsCreatedAfter(any())).thenReturn(List.of());

        // inactive 사용자: 최근 6개월 내 새 리뷰가 없는 사용자 반환.
        // 예: 사용자 초기 점수가 50이면, DECAY_RATE=5, BASE_SCORE=30이므로 50-5 = 45
        when(userService.getUsersWithoutReviewsSince(any())).thenReturn(List.of(mockUser));
        when(mockUser.getScore()).thenReturn(50);

        reviewScoreScheduler.aggregateReviewScores();

        // 새 리뷰가 없으므로 집계 부분에서는 업데이트가 이루어지 없고, 감쇠 로직에서 updateUserScore가 호출됨.
        // 50 -> 45 (감쇠)
        verify(userService, times(1)).updateUserScore(eq(1L), eq(45.0));
    }

    @Test
    @DisplayName("calculateWeight 테스트")
    void calculateWeightTest() {
        double weight1 = ScheduleUtil.calculateWeight(LocalDateTime.now().minusDays(10));
        double weight2 = ScheduleUtil.calculateWeight(LocalDateTime.now().minusMonths(3));
        double weight3 = ScheduleUtil.calculateWeight(LocalDateTime.now().minusMonths(11));

        assertEquals(1.0, weight1);
        assertEquals(0.7, weight2);
        assertEquals(0.3, weight3);
    }

    @Test
    @DisplayName("determineScoreChange 테스트")
    void determineScoreChangeTest() {
        double change1 = ScheduleUtil.determineScoreChange(1.5);
        double change2 = ScheduleUtil.determineScoreChange(2.5);
        double change3 = ScheduleUtil.determineScoreChange(3.5);
        double change4 = ScheduleUtil.determineScoreChange(4.5);
        double change5 = ScheduleUtil.determineScoreChange(5.5); // 비정상 리뷰

        assertEquals(-1.0, change1);
        assertEquals(-0.5, change2);
        assertEquals(0.5, change3);
        assertEquals(1.0, change4);
        assertEquals(0.0, change5);
    }

    @Test
    @DisplayName("재시도 로직 테스트 (3회 재시도)")
    void retryTest() {
        when(schedulerLockRepository.findByLockNameWithLock(anyString())).thenReturn(Optional.empty());
        when(reviewService.getReviewsCreatedAfter(any())).thenThrow(new RuntimeException("리뷰 조회 예외"));

        reviewScoreScheduler.aggregateReviewScores();

        verify(reviewService, times(3)).getReviewsCreatedAfter(any());
        verify(schedulerLockRepository, atLeast(3)).findByLockNameWithLock(anyString());
        verify(schedulerLockRepository, atLeast(3)).save(any());
        verify(schedulerLockRepository, atLeast(3)).deleteById(anyString());
        verify(userService, never()).updateUserScore(anyLong(), anyDouble());
    }
}
