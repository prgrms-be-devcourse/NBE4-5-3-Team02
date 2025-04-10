package com.snackoverflow.toolgether.global.config

import com.snackoverflow.toolgether.global.filter.GoogleAccessTokenFilter
import com.snackoverflow.toolgether.global.filter.JwtAuthenticationFilter
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
internal class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val googleAccessTokenFilter: GoogleAccessTokenFilter
) {
    @Value("\${custom.site.frontUrl}")
    lateinit var allowedOrigins: Array<String>

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors: CorsConfigurer<HttpSecurity?> -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests{ authorize ->
                authorize
                    .anyRequest().permitAll()
            } // 모든 요청 허용
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(googleAccessTokenFilter, JwtAuthenticationFilter::class.java)
            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity?> ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            }
            .headers { headers: HeadersConfigurer<HttpSecurity?> ->
                headers
                    .addHeaderWriter(
                        XFrameOptionsHeaderWriter(
                            XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                        )
                    )
            }
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()
        // 허용할 오리진 설정
        configuration.allowedOrigins = listOf(*allowedOrigins)
        // 허용할 HTTP 메서드 설정
        configuration.allowedMethods = mutableListOf("GET", "POST", "PUT", "DELETE", "PATCH")
        // 자격 증명 허용 설정
        configuration.allowCredentials = true
        // 허용할 헤더 설정
        configuration.allowedHeaders = listOf("*")
        // CORS 설정을 소스에 등록
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}