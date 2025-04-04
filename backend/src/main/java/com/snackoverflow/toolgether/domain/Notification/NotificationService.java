package com.snackoverflow.toolgether.domain.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserService userService;
	private final ApplicationEventPublisher publisher;

	// 알림 생성 및 저장
	@Transactional
	public Notification createNotification(Long userId, String message) {
		Notification notification = Notification.builder()
			.user(userService.findUserById(userId))
			.message(message)
			.createdAt(LocalDateTime.now())
			.isRead(false)
			.build();
		Notification savedNotification = notificationRepository.save(notification);

		// 이벤트 발행
		publisher.publishEvent(new NotificationCreatedEvent(this, userId, message));
		return savedNotification;
	}

	// 사용자 ID로 읽지 않은 알림 목록 조회
	@Transactional(readOnly = true)
	public List<NotificationDto> getUnreadNotifications(Long userId) {
		return notificationRepository.findByUserIdAndIsRead(userId, false)
			.stream()
			.map(NotificationDto::new)
			.collect(Collectors.toList());
	}

	@Transactional
	public void markNotificationAsRead(Long notificationId) {
		Optional<Notification> notification = notificationRepository.findById(notificationId);
		if (notification.isPresent()) {
			Notification n = notification.get();
			n.setIsRead(true);
			notificationRepository.save(n);
		} else {
			throw new RuntimeException("Notification not found");
		}
	}
}
