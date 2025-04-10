package com.snackoverflow.toolgether.global.init;

import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus;
import com.snackoverflow.toolgether.domain.deposit.entity.ReturnReason;
import com.snackoverflow.toolgether.domain.deposit.repository.DepositHistoryRepository;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;
import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.repository.ReviewRepository;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
	private final DepositHistoryRepository depositHistoryRepository;

	@Autowired
	@Lazy
	private BaseInitData self;

    @PersistenceContext
    private EntityManager entityManager;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			self.reservationInit();
		};
	}

	@Transactional
	public void reservationInit() {

//        UserInitData 전부 삭제 후 재생성 코드(AUTO_INCREMENT 초기화와 함께 주석 풀고 사용)
//        reviewRepository.deleteAll();
//        reservationRepository.deleteAll();
//        postRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // AUTO_INCREMENT 초기화
//        entityManager.createNativeQuery("ALTER TABLE review AUTO_INCREMENT = 1").executeUpdate();
//        entityManager.createNativeQuery("ALTER TABLE reservation AUTO_INCREMENT = 1").executeUpdate();
//        entityManager.createNativeQuery("ALTER TABLE post AUTO_INCREMENT = 1").executeUpdate();
//        entityManager.createNativeQuery("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();

        if(userRepository.count() > 0) {
			return;
		}
		// 비밀번호 암호화
		String password = passwordEncoder.encode("password123");

		val user1 = User.createGeneralUser(
				"user1@example.com",
				password,
				"01012345678",
				"UserOne",
				"서울시 서초구");

		userRepository.save(user1);

		val user2 = User.createGeneralUser(
				"user2@example.com",
				password,
				"01098765432",
				"UserTwo",
				"서울시 성동구");

		userRepository.save(user2);

		val user3 = User.createSocialUser(
				"12345",
				"Google",
				"01011112222",
				"socialuser1@example.com",
				"SocialUserOne",
				"서울 관악구");

		userRepository.save(user3);

		val user4 = User.createSocialUser(
				"67890",
				"Google",
				"01033334444",
				"socialuser2@example.com",
				"SocialUserTwo",
				"서울 마포구"
		);

		userRepository.save(user4);

		Set<PostAvailability> sp = new HashSet<>();
		Post post = Post.createPost(
				user1,
				"제목입니다.",
				"내용입니다.",
				Category.TOOL,
				PriceType.DAY,
				10000,
				37.5665,
				126.9780
		);

		Post savedPost = postRepository.save(post); // post 를 먼저 저장하고 저장된 post를 받음

		PostAvailability sp1 = PostAvailability.builder()
			.isRecurring(false)
			.recurrence_days(0)
			.date(LocalDateTime.of(2025, 3, 25, 0, 0, 0))
			.startTime(LocalDateTime.of(2025, 3, 25, 10, 0, 0))
			.endTime(LocalDateTime.of(2025, 3, 25, 17, 0, 0))
			.post(savedPost) // Post 객체 할당
			.build();
		sp.add(sp1);

		savedPost.updatePostAvailability(sp); // post에 postAvailability 설정
		postRepository.save(savedPost); // post를 다시 저장.

		Post post2 = Post.createPost(
				user2,
				"부산에서 빌려드려요.",
				"해운대 근처에서 사용할 수 있는 드릴입니다.",
				Category.TOOL,
				PriceType.HOUR,
				5000,
				35.1587,
				129.1600
		);
		postRepository.save(post2);

		Post post3 = Post.createPost(
				user1,
				"전동 드릴 대여",
				"상태 좋은 전동 드릴 빌려드립니다.",
				Category.TOOL,
				PriceType.DAY,
				10000,
				37.123,
				127.123
		);
        postRepository.saveAndFlush(post3);

		Post post4 = Post.createPost(
				user2,
				"캠핑 의자 세트 대여",
				"편안한 캠핑 의자 세트 저렴하게 빌려가세요.",
				Category.TOOL,
				PriceType.DAY,
				5000,
				37.456,
				127.456
		);
        postRepository.saveAndFlush(post4);

        // Reservation 데이터 생성
        Reservation reservation1 = new Reservation(
			post3,
			user2,
			user1,
			LocalDateTime.now(),
			LocalDateTime.now().plusDays(1),
			LocalDateTime.now().plusDays(3),
			ReservationStatus.APPROVED,
			"None",
			20000.0
		);
        Reservation savedReservation1 = reservationRepository.saveAndFlush(reservation1);

		DepositHistory depositHistory1 = new DepositHistory(
			savedReservation1,
			user1,
			10000,
			DepositStatus.PENDING,
			ReturnReason.NONE
		);
		depositHistoryRepository.save(depositHistory1);

        Reservation reservation2 = new Reservation(
			post4,
			user1,
			user2,
			LocalDateTime.now(),
			LocalDateTime.now().plusDays(2),
			LocalDateTime.now().plusDays(5),
			ReservationStatus.DONE,
			"None",
			15000.0
		);
        Reservation savedReservation2 = reservationRepository.saveAndFlush(reservation2);

		DepositHistory depositHistory2 = new DepositHistory(
			savedReservation2,
			user2,
			10000,
			DepositStatus.PENDING,
			ReturnReason.NONE
		);
		depositHistoryRepository.save(depositHistory2);

        // Review 데이터 생성 (reservation2에 대한 리뷰)
		Review review1 = new Review(
				null,
				user2,
				user1,
				reservation2,
				5,
				5,
				5,
				null
		);
//        Review review1 = Review.builder()
//                .reviewer(user2) // user2가 작성
//                .reviewee(user1) // user1에게 리뷰
//                .reservation(reservation2)
//                .productScore(5)
//                .timeScore(5)
//                .kindnessScore(5)
//                .build();
        reviewRepository.saveAndFlush(review1);

        // Review 데이터 생성 (reservation2에 대한 리뷰)
//        Review review2 = Review.builder()
//                .reviewer(user1) // user1이 작성
//                .reviewee(user2) // user2에게 리뷰
//                .reservation(reservation2)
//                .productScore(4)
//                .timeScore(4)
//                .kindnessScore(4)
//                .build();
//        reviewRepository.saveAndFlush(review2);
	}
}
