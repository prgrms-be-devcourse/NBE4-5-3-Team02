package com.snackoverflow.toolgether.domain.Notification.dto;

import com.snackoverflow.toolgether.domain.Notification.entity.Notification;

public class NotificationDto {
	public Long id;
	public String message;

	public NotificationDto(Notification notification) {
		this.id = notification.getId();
		this.message = notification.getMessage();
	}
}
