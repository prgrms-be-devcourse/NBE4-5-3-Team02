package com.snackoverflow.toolgether.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.Executor;

import static com.snackoverflow.toolgether.domain.user.service.VerificationService.SESSION_KEY;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 기본 스레드 수
        executor.setMaxPoolSize(30); // 최대 스레드 수
        executor.setQueueCapacity(50); // 대기 큐 크기
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 식별자
        executor.setTaskDecorator(new SessionAwareTaskDecorator());
        executor.initialize(); // 초기화
        return executor;
    }

    // 세션 복제 데코레이터
    public class SessionAwareTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = request.getSession(false);

            return () -> {
                try {
                    RequestContextHolder.setRequestAttributes(
                            new ServletRequestAttributes(request)
                    );
                    if (session != null) {
                        request.getSession().setAttribute(SESSION_KEY, session.getAttribute(SESSION_KEY));
                    }
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
