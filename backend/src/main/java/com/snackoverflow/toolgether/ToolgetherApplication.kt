package com.snackoverflow.toolgether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
class ToolgetherApplication

fun main(args: Array<String>) {
	runApplication<ToolgetherApplication>(*args)
}
