package com.snackoverflow.toolgether.global.init;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;
import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.repository.ReviewRepository;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

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

//        UserInitData 전부 삭제 후 재생성 코드
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
		User user1 = User.builder()
			.address(new Address("서울", "강남구", "12345")) // Address 객체 생성 및 설정
			.username("human123")
			.nickname("사람이")
			.password("12345678")
			.score(30)
			.phoneNumber("01012345678")
			.latitude(37.5665)
			.longitude(126.9780)
			.build();
		userRepository.save(user1);
		postRepository.save(Post.builder()
			.user(user1)
			.title("제목입니다.")
			.content("내용입니다.")
			.category(Category.TOOL)
			.priceType(PriceType.DAY)
			.price(10000)
			.latitude(37.5665)
			.longitude(126.9780)
			.build());

		User user2 = User.builder()
			.address(new Address("부산", "해운대구", "67890"))
			.username("seaman222")
			.nickname("바다사람")
			.password("56781234")
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
        Reservation reservation1 = Reservation.builder()
                .post(post3)
                .renter(user2)
                .owner(user1)
                .createAt(LocalDateTime.now())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(3))
                .status(ReservationStatus.APPROVED)
                .amount(20000.0)
                .build();
        reservationRepository.saveAndFlush(reservation1);

        Reservation reservation2 = Reservation.builder()
                .post(post4)
                .renter(user1)
                .owner(user2)
                .createAt(LocalDateTime.now())
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(5))
                .status(ReservationStatus.DONE)
                .amount(15000.0)
                .build();
        reservationRepository.saveAndFlush(reservation2);

        // Review 데이터 생성 (reservation2에 대한 리뷰)
        Review review1 = Review.builder()
                .reviewer(user2) // user2가 작성
                .reviewee(user1) // user1에게 리뷰
                .reservation(reservation2)
                .productScore(5)
                .timeScore(5)
                .kindnessScore(5)
                .build();
        reviewRepository.saveAndFlush(review1);

        // Review 데이터 생성 (reservation2에 대한 리뷰)
        Review review2 = Review.builder()
                .reviewer(user1) // user1이 작성
                .reviewee(user2) // user2에게 리뷰
                .reservation(reservation2)
                .productScore(4)
                .timeScore(4)
                .kindnessScore(4)
                .build();
        reviewRepository.saveAndFlush(review2);
	}
}
