package com.snackoverflow.toolgether.domain.notification.entity

import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User, @Column(nullable = false)

    var message: String,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime,

    @Column(nullable = false)
    private var isRead: Boolean
) {
    constructor() : this(null, User(), "", LocalDateTime.now(), false)
    constructor(user: User?, message: String, createdAt: LocalDateTime, isRead: Boolean) : this()

    fun setIsRead(isRead: Boolean) {
        this.isRead = isRead
    }
}