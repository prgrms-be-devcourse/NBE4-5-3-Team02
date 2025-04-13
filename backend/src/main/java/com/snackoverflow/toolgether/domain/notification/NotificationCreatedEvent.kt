package com.snackoverflow.toolgether.domain.notification

import org.springframework.context.ApplicationEvent

class NotificationCreatedEvent(source: Any, val userId: Long, val message: String) : ApplicationEvent(source)