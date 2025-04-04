package com.snackoverflow.toolgether.domain.reservation.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/sse")
public class SseController {

	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	@GetMapping(value = "/connect/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(@PathVariable Long userId) {
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

		emitters.put(userId, emitter);

		emitter.onCompletion(() -> {
			emitters.remove(userId);
			log.info("SSE connection completed for user: {}", userId); // 로그 추가
		});

		emitter.onTimeout(() -> {
			emitters.remove(userId);
			log.warn("SSE connection timed out for user: {}", userId); // 로그 추가
		});

		log.info("SSE connection established for user: {}", userId); // 로그 추가
		return emitter;
	}

	public void sendNotification(Long userId, String message) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("reservationUpdate").data(message));
				log.info("SSE notification sent to user {}: {}", userId, message); // 로그 추가
			} catch (IOException e) {
				emitters.remove(userId);
				log.error("Error sending SSE notification to user: {}", userId, e); // 로그 추가
			}
		} else {
			log.warn("No SSE emitter found for user: {}", userId); // 로그 추가
		}
	}
}
