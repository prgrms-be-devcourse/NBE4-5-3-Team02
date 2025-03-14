package com.snackoverflow.toolgether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class ToolgetherApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToolgetherApplication.class, args);
	}

}
