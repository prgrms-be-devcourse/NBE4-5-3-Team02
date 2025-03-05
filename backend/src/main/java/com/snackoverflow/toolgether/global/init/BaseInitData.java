package com.snackoverflow.toolgether.global.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.post.entity.Category;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.PriceType;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
	private final PostRepository postRepository;
	private final UserRepository userRepository;

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
		if(userRepository.count() > 0) {
			return;
		}
		User user = User.builder()
			.address(new Address("서울", "강남구", "12345")) // Address 객체 생성 및 설정
			.nickname("사람")
			.password("1234")
			.score(30)
			.phoneNumber("01012345678")
			.latitude(37.5665)
			.longitude(126.9780)
			.build();
		userRepository.save(user);
		postRepository.save(Post.builder()
			.user(user)
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
			.nickname("바다사람")
			.password("5678")
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
	}
}
