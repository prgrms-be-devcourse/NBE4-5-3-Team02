package com.snackoverflow.toolgether.domain.Notification;

public class NotificationDto {
	public Long id;
	public String message;

	public NotificationDto(Notification notification) {
		this.id = notification.getId();
		this.message = notification.getMessage();
	}
}
