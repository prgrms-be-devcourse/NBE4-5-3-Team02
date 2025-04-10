package com.snackoverflow.toolgether.global.config;

import com.snackoverflow.toolgether.global.constants.AppConstants.SESSION_KEY
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.concurrent.Executor


@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    @Bean
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10 // 기본 스레드 수
        executor.maxPoolSize = 30 // 최대 스레드 수
        executor.queueCapacity = 50 // 대기 큐 크기
        executor.threadNamePrefix = "Async-" // 스레드 이름 식별자
        executor.setTaskDecorator(SessionAwareTaskDecorator())
        executor.initialize() // 초기화
        return executor
    }

    // 세션 복제 데코레이터
    class SessionAwareTaskDecorator : TaskDecorator {
        override fun decorate(runnable: Runnable): Runnable {
            val request =
                (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
            val session = request.getSession(false)

            return Runnable {
                try {
                    RequestContextHolder.setRequestAttributes(
                        ServletRequestAttributes(request)
                    )
                    if (session != null) {
                        request.session.setAttribute(
                            SESSION_KEY,
                            session.getAttribute(SESSION_KEY)
                        )
                    }
                    runnable.run()
                } finally {
                    RequestContextHolder.resetRequestAttributes()
                }
            }
        }
    }
}