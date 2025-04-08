package com.snackoverflow.toolgether.domain.notification.entity

import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    lateinit var user: User

    @Column(nullable = false)
    lateinit var message: String

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @Column(nullable = false)
    private var isRead = false

    constructor(user: User, message: String, createdAt: LocalDateTime, isRead: Boolean) {
        this.user = user
        this.message = message
        this.createdAt = createdAt
        this.isRead = isRead
    }

    fun setIsRead(isRead: Boolean) {
        this.isRead = isRead
    }
}