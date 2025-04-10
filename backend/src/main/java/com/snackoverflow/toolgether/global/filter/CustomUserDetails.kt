package com.snackoverflow.toolgether.global.filter;

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(
        private val id: Long,
        private val email: String
) : UserDetails {

    val userId: Long get() = id
    val userEmail: String get() = email

    /**
     * 사용자에게 기본 ROLE_USER 권한 부여
     * 추후 관리자 기능 추가 시 동적 권한 할당 가능
     */
    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    /*
    override fun getAuthorities(): Collection<GrantedAuthority> {
        // 사용자 역할에 따라 권한 설정 (추후 확장 가능)
        val authorities = mutableListOf<GrantedAuthority>()
        if (userRole == "ADMIN") {
            authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
        }
        authorities.add(SimpleGrantedAuthority("ROLE_USER"))
        return authorities
    }
    */

    /**
     * 비밀번호는 빈 문자열 반환 (비밀번호가 필요 없는 경우)
     */
    override fun getPassword(): String = ""

    /**
     * 사용자 이메일을 username 으로 사용
     */
    override fun getUsername(): String = email

    /**
     * 계정이 만료되지 않았음을 나타냄
     */
    override fun isAccountNonExpired(): Boolean = true

    /**
     * 계정이 잠겨 있지 않았음을 나타냄
     */
    override fun isAccountNonLocked(): Boolean = true

    /**
     * 자격 증명이 만료되지 않았음을 나타냄
     */
    override fun isCredentialsNonExpired(): Boolean = true

    /**
     * 계정이 활성화되어 있음을 나타냄
     */
    override fun isEnabled(): Boolean = true
}