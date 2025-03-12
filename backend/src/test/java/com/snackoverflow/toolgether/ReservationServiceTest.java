package com.snackoverflow.toolgether;

// ReservationServiceTest.java (src/test/java/...  패키지)

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser; // 추가
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes; //추가

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicReference;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
class ReservationServiceTest {

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	private User owner;
	private User renter;
	private Post post;

	@TestConfiguration
	static class TestConfig{
		@Bean //추가
		public RequestContextListener requestContextListener() {
			return new RequestContextListener();
		}
	}

	@BeforeEach
	void setUp() {
		owner = User.builder()
			.address(new Address("서울", "강남구", "12345")) // Address 객체 생성 및 설정
			.username("testman1")
			.nickname("실험맨1")
			.email("test1@gmail.com")
			.password("1234")
			.score(30)
			.phoneNumber("01009090909")
			.latitude(37.5665)
			.longitude(126.9780)
			.build();

		renter = User.builder()
			.address(new Address("부산", "해운대구", "67890"))
			.username("testman2")
			.nickname("실험맨2")
			.email("test2@gmail.com")
			.password("5678")
			.score(50)
			.phoneNumber("01008080808")
			.latitude(35.1587)
			.longitude(129.1600)
			.build();

		// Post 엔티티의 생성자에 User 객체 할당.
		post = Post.builder()
			.user(owner) // owner를 먼저 영속화해야 Post에 넣을 수 있음.
			.title("전동 드릴 대여")
			.content("상태 좋은 전동 드릴 빌려드립니다.")
			.category(Category.TOOL)
			.priceType(PriceType.DAY)
			.price(10000)
			.latitude(37.123)
			.longitude(127.123)
			.build();
		//영속화 순서 중요!
		owner = userRepository.save(owner);
		renter = userRepository.save(renter);
		post = postRepository.save(post); // post 저장
	}

	@Test
		//@WithMockUser // 가짜 사용자 인증 정보 설정 -> 필요 없어짐. findReservationByIdOrThrow 수정했으므로
	void approveReservation_OptimisticLockingFailure() throws InterruptedException {
		// AtomicReference를 사용하여 예외를 저장
		AtomicReference<Throwable> exceptionHolder = new AtomicReference<>();

		// When: 두 개의 스레드에서 동시에 approveReservation 호출
		Runnable approveTask = () -> {
			try {
				// *각 스레드*에서 Reservation 객체 생성 및 저장
				Reservation reservation = Reservation.builder() //여기서 reservation 객체 생성
					.owner(owner)
					.renter(renter)
					.post(post)
					.status(ReservationStatus.REQUESTED)
					.startTime(LocalDateTime.now().plusHours(1))
					.endTime(LocalDateTime.now().plusHours(2))
					.amount(10000.0)
					.build();
				reservation = reservationRepository.save(reservation); // 각 스레드에서 저장
				reservationService.approveReservation(reservation.getId()); // 각 스레드에서 approveReservation
			} catch (Throwable e) { // 수정: 모든 예외를 잡도록 변경
				// 동시성 예외 발생하면 저장
				exceptionHolder.set(e); // AtomicReference에 예외 저장
			}
		};

		Thread thread1 = new Thread(approveTask);
		Thread thread2 = new Thread(approveTask);
		thread1.start();
		thread2.start();

		thread1.join(); // 스레드1 대기
		thread2.join(); // 스레드2 대기

		assertThrows(Exception.class, () -> {
			Throwable exception = exceptionHolder.get();
			if (exception != null) {
				throw exception;
			}
		});
	}
}