package com.snackoverflow.toolgether.domain.Notification;

import org.springframework.context.ApplicationEvent;

public class NotificationCreatedEvent extends ApplicationEvent {
	private final Long userId;
	private final String message;

	public NotificationCreatedEvent(Object source, Long userId, String message) {
		super(source);
		this.userId = userId;
		this.message = message;
	}

	public Long getUserId() {
		return userId;
	}

	public String getMessage() {
		return message;
	}
}