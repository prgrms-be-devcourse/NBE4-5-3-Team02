package com.snackoverflow.toolgether.global.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String username;
    private String email;
    private Long userId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자에게 ROLE_USER 권한 부여 -> 추후 관리자 기능 추가 시에 동적 권한 할당
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

/*    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 역할에 따라 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userRole.equals("ADMIN")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }*/

    @Override
    public String getPassword() {
        return "";
    }

    /* TODO: 코틀린 변환 전 임시 게터. 추후 제거하고 사용하는 부분 수정 */
    public Long getUserId() {
        return this.userId;
    }
}
