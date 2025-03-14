package com.snackoverflow.toolgether.domain.review.scheduler;

import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.scheduler.lock.entity.SchedulerLock;
import com.snackoverflow.toolgether.domain.review.scheduler.lock.repository.SchedulerLockRepository;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.util.ScheduleUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 10000))
public class ReviewScoreScheduler {
    private final ReviewService reviewService;
    private final UserService userService;
    private final SchedulerLockRepository schedulerLockRepository;

    private static final String LOCK_NAME = "aggregateLock";

    // 감쇠 관련 상수
    // 6개월 이내 리뷰가 없을 경우 매주 감점할 점수
    private static final double DECAY_RATE = 5.0;
    // 기본 점수
    private static final double BASE_SCORE = 30.0;

    @Scheduled(cron = "0 0 0 ? * MON")
    @Transactional
    public void aggregateReviewScores() {
        System.out.println("------------점수 집계 작업 작동------------");

        // 락 획득 시도
        Optional<SchedulerLock> lockOptional = schedulerLockRepository.findByLockNameWithLock(LOCK_NAME);
        if (lockOptional.isPresent()) {
            System.out.println("------------락 획득 실패 - 이미 실행 중------------");
            return;
        }
        System.out.println("------------락 획득 성공------------");

        SchedulerLock lock = new SchedulerLock();
        lock.setLockName(LOCK_NAME);
        lock.setLockedAt(LocalDateTime.now());
        schedulerLockRepository.save(lock);
        System.out.println("------------점수 집계 시작------------");

        try {
            // 새 리뷰 집계: 지난 1주일 동안 생성된 리뷰만 대상
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            List<Review> newReviews = reviewService.getReviewsCreatedAfter(oneWeekAgo);
            System.out.println("새롭게 집계할 리뷰 개수: %d".formatted(newReviews.size()));

            // 새 리뷰가 있다면, 사용자별로 그룹화하고, 각 리뷰별로 점수 변동폭을 산출하여 합산
            if (!newReviews.isEmpty()) {
                Map<User, List<Review>> reviewsByUser = newReviews.stream()
                        .collect(Collectors.groupingBy(Review::getReviewee));
                System.out.println("새 리뷰를 가진 사용자 수: %d".formatted(reviewsByUser.size()));

                reviewsByUser.forEach((user, reviews) -> {
                    double totalChange = 0;
                    for (Review review : reviews) {
                        // 단건 리뷰의 평균 점수 계산
                        double reviewAvg = (review.getProductScore() + review.getTimeScore() + review.getKindnessScore()) / 3.0;
                        // 리뷰에 대해 결정된 점수 변화량 (예: 3점 -> +0.5, 5점 -> +1.0, 2점 -> -0.5)
                        double change = ScheduleUtil.determineScoreChange(reviewAvg);
                        totalChange += change;
                    }
                    double updatedScore = Math.max(0, Math.min(100, user.getScore() + totalChange));
                    System.out.println("유저id: " + user.getId() +
                            ", 기존 점수: " + user.getScore() +
                            ", 새 리뷰 총 변화량: " + totalChange +
                            ", 업데이트 후 점수: " + updatedScore);
                    userService.updateUserScore(user.getId(), updatedScore);
                });
            } else {
                System.out.println("해당 유저의 지난 1주일 동안 새 리뷰가 없습니다.");
            }

            // 감쇠 로직: 최근 6개월 동안 새 리뷰가 전혀 없는 사용자에 대해, 매주 5점씩 감점 (단, 점수는 30점 이하로 내려가지 않음)
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            List<User> inactiveUsers = userService.getUsersWithoutReviewsSince(sixMonthsAgo);
            System.out.println("최근 6개월 새 리뷰가 없는 사용자 수: " + inactiveUsers.size());
            for (User user : inactiveUsers) {
                if (user.getScore() > BASE_SCORE) {
                    double decayedScore = Math.max(BASE_SCORE, user.getScore() - DECAY_RATE);
                    userService.updateUserScore(user.getId(), decayedScore);
                }
            }

            System.out.println("------------점수 집계 작업 완료------------");
        } finally {
            // 락 해제
            schedulerLockRepository.deleteById(LOCK_NAME);
            System.out.println("------------락 해제------------");
        }
    }

    @Recover
    public void recover(Exception e) {
        System.err.println("------------점수 집계 작업 최종 실패!------------");
        System.err.println("에러 내용: " + e.getMessage());
        System.err.println("실패 시간: " + LocalDateTime.now());
        System.err.println("------------원인을 파악해 수정하거나 집계를 수동으로 재시도하세요.------------");
    }
}
