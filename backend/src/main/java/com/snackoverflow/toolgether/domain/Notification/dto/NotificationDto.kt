package com.snackoverflow.toolgether.domain.notification.dto

import com.snackoverflow.toolgether.domain.notification.entity.Notification

class NotificationDto(notification: Notification) {
    var id: Int? = notification.id
    var message: String = notification.message
}