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
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
		String password1 = "password123";
		password1 = passwordEncoder.encode(password1);
		User user1 = User.builder()
			.address(new Address("서울", "강남구", "12345")) // Address 객체 생성 및 설정
			.username("human123")
			.nickname("사람이")
			.email("human123@gmail.com")
			.password(password1)
			.score(30)
			.phoneNumber("01012345678")
			.latitude(37.5665)
			.longitude(126.9780)
			.build();
		userRepository.save(user1);
		Set<PostAvailability> sp = new HashSet<>();
		Post post = Post.builder() // Post 객체를 먼저 생성
			.user(user1)
			.title("제목입니다.")
			.content("내용입니다.")
			.category(Category.TOOL)
			.priceType(PriceType.DAY)
			.price(10000)
			.latitude(37.5665)
			.longitude(126.9780)
			.build();
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
		savedPost.setPostAvailabilities(sp); // post에 postAvailability 설정
		postRepository.save(savedPost); // post를 다시 저장.

		String password2 = "password123";
		password2 = passwordEncoder.encode(password2);

		User user2 = User.builder()
			.address(new Address("부산", "해운대구", "67890"))
			.username("seaman222")
			.nickname("바다사람")
			.email("seaman222@gmail.com")
			.password(password2)
			.score(50)
			.phoneNumber("01098765432")
			.latitude(35.1587)
			.longitude(129.1600)
			.build();
		userRepository.save(user2);
		Post post2 = Post.builder()
			.user(user2)
			.title("부산에서 빌려드려요.")
			.content("해운대 근처에서 사용할 수 있는 드릴입니다.")
			.category(Category.TOOL)
			.priceType(PriceType.HOUR)
			.price(5000)
			.latitude(35.1587)
			.longitude(129.1600)
			.build();
		postRepository.save(post2);

        User googleUser = User.builder()
                .username(null)
                .password(null)
                .nickname("googleUser")
                .email("googleuser@gmail.com")
                .phoneNumber("000-0000-0004")
                .address(new Address("서울시 종로구", "청진동 101-11", "10111"))
                .latitude(37.123)
                .longitude(127.123)
                .provider("google")
                .providerId("google123456789")
                .build();
        userRepository.saveAndFlush(googleUser);
        Post post3 = Post.builder()
                .user(user1)
                .title("전동 드릴 대여")
                .content("상태 좋은 전동 드릴 빌려드립니다.")
                .category(Category.TOOL)
                .priceType(PriceType.DAY)
                .price(10000)
                .latitude(37.123)
                .longitude(127.123)
                .build();
        postRepository.saveAndFlush(post3);

        Post post4 = Post.builder()
                .user(user2)
                .title("캠핑 의자 세트 대여")
                .content("편안한 캠핑 의자 세트 저렴하게 빌려가세요.")
                .category(Category.TOOL)
                .priceType(PriceType.DAY)
                .price(5000)
                .latitude(37.456)
                .longitude(127.456)
                .build();
        postRepository.saveAndFlush(post4);

        // Reservation 데이터 생성
        Reservation reservation1 = new Reservation(
			post3,
			user2,
			user1,
			LocalDateTime.now(),
			LocalDateTime.now().plusDays(1),
			LocalDateTime.now().plusDays(3),
			ReservationStatus.REQUESTED,
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
			ReservationStatus.APPROVED,
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
