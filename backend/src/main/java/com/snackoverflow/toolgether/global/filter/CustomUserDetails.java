package com.snackoverflow.toolgether.global.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String username;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 권한이 필요 없으므로 빈 리스트 반환
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return "";
    }
}
