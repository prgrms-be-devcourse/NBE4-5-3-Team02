package com.snackoverflow.toolgether.domain.Notification;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.snackoverflow.toolgether.domain.reservation.controller.SseController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationListener implements ApplicationListener<NotificationCreatedEvent> {

	private final SseController sseController;

	@Override
	public void onApplicationEvent(NotificationCreatedEvent event) {
		Long userId = event.getUserId();
		String message = event.getMessage();
		log.info("Received NotificationCreatedEvent. Sending SSE notification to user {}: {}", userId, message);
		sseController.sendNotification(userId, message);
	}
}
