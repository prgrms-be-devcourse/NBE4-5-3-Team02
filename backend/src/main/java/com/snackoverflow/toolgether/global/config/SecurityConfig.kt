package com.snackoverflow.toolgether.global.config;

import com.snackoverflow.toolgether.global.filter.CustomAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationFilter: CustomAuthenticationFilter,
    @Value("\${custom.dev.frontUrl}") private val allowOrigin: String,
) {


    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors { cors -> cors.configurationSource(corsConfigurationSource()) }.authorizeHttpRequests { authorize ->
                authorize.requestMatchers("/").permitAll().requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/api/v2/users/**").permitAll().requestMatchers("/api/chat/**").permitAll()
                    .requestMatchers("/api/*/**").permitAll().anyRequest().permitAll() // 모든 요청 허용
            }.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers ->
                headers.addHeaderWriter(XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN))
            }.csrf { it.disable() }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(allowOrigin)// 허용할 오리진 설정
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH") // 허용할 HTTP 메서드 설정
            allowCredentials = true // 자격 증명 허용 설정
            allowedHeaders = listOf("*") // 허용할 헤더 설정
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration) // CORS 설정을 소스에 등록
        }
    }
}