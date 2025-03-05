package com.snackoverflow.toolgether.global.init;

import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        userRepository.deleteAll();
        String encoded = passwordEncoder.encode("test1111");
        User testUser = User.builder()
                .username("exampleTest1")
                .password(encoded)
                .email("exampleForTest@gmail.com")
                .phoneNumber("01012345678")
                .nickname("성수동거주민")
                .address(Address.builder()
                        .mainAddress("서울시 성동구 성수동")
                        .detailAddress("성수아파트 101동 202호")
                        .zipcode("04700")
                        .build())
                .latitude(37.544579)
                .longitude(127.055961)
                .build();
        userRepository.save(testUser);
    }
}
