package com.snackoverflow.toolgether.global.filter;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
class LoginUserArgumentResolver : HandlerMethodArgumentResolver {
    // @Login 애노테이션이 달려있는지 확인
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(Login::class.java) && (
                CustomUserDetails::class.java.isAssignableFrom(parameter.getParameterType()))
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null) {
            throw AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.")
        }
        val principal = authentication.principal

        if (principal !is CustomUserDetails) {
            throw AuthenticationCredentialsNotFoundException("인증 정보가 잘못되었습니다.");
        }
        return principal
    }
}