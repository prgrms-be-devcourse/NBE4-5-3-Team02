package com.snackoverflow.toolgether.domain.post.entity

import com.snackoverflow.toolgether.domain.post.entity.enums.Category
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage
import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Column(nullable = false)
    var title: String, // 제목
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String, // 내용
    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime? = null, // 글 작성 시간
    @UpdateTimestamp
    var updateAt: LocalDateTime? = null, // 글 수정 시간
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: Category, // 카테고리 (TOOL, ELECTRONICS)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var priceType: PriceType, // 가격 유형 (일 / 시간)
    @Column(nullable = false)
    var price: Int = 0, // 총 가격
    @Column(nullable = false)
    var latitude: Double, // 위도
    @Column(nullable = false)
    var longitude: Double, // 경도
    var viewCount: Int = 0, // 조회수 (기본 0)
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 10)
    val postImages: MutableSet<PostImage> = HashSet(), // 이미지
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 10)
    var postAvailabilities: MutableSet<PostAvailability> = HashSet() // 스케줄
) {
    /* TODO : 마이그레이션 이후 삭제 */
    // 파라미터 없는 기본 생성자 추가
    constructor() : this(
        null,
        null,
        "",
        "",
        null,
        null,
        Category.TOOL,
        PriceType.DAY,
        0,
        0.0,
        0.0,
        0,
        HashSet(),
        HashSet()
    )

    // 나머지 메서드 (updatePost, incrementViewCount, setPostAvailabilities 등)

    fun updatePost(
        title: String,
        content: String,
        category: Category,
        priceType: PriceType,
        price: Int,
        latitude: Double,
        longitude: Double,
        viewCount: Int
    ) {
        this.title = title
        this.content = content
        this.category = category
        this.priceType = priceType
        this.price = price
        this.latitude = latitude
        this.longitude = longitude
        this.viewCount = viewCount
    }

    fun incrementViewCount() {
        this.viewCount++
    }

//    fun setPostAvailabilities(
//        postAvailabilities: MutableSet<PostAvailability>
//    ) {
//        this.postAvailabilities = postAvailabilities
//    }

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var id: Long? = null
        private var user: User? = null
        private lateinit var title: String
        private lateinit var content: String
        private lateinit var category: Category
        private lateinit var priceType: PriceType
        private var price: Int = 0
        private var latitude: Double = 0.0
        private var longitude: Double = 0.0
        private var viewCount: Int = 0
        private val postImages: MutableSet<PostImage> = HashSet()
        private val postAvailabilities: MutableSet<PostAvailability> = HashSet()
        private var createdAt: LocalDateTime? = null
        private var updateAt: LocalDateTime? = null

        fun id(id: Long) = apply { this.id = id }
        fun user(user: User) = apply { this.user = user }
        fun title(title: String) = apply { this.title = title }
        fun content(content: String) = apply { this.content = content }
        fun category(category: Category) = apply { this.category = category }
        fun priceType(priceType: PriceType) = apply { this.priceType = priceType }
        fun price(price: Int) = apply { this.price = price }
        fun latitude(latitude: Double) = apply { this.latitude = latitude }
        fun longitude(longitude: Double) = apply { this.longitude = longitude }
        fun viewCount(viewCount: Int) = apply { this.viewCount = viewCount }
        fun postImages(postImages: Set<PostImage>) = apply { this.postImages.addAll(postImages) }
        fun postAvailabilities(postAvailabilities: Set<PostAvailability>) = apply { this.postAvailabilities.addAll(postAvailabilities) }
        fun createdAt(createdAt: LocalDateTime?) = apply { this.createdAt = createdAt }
        fun updateAt(updateAt: LocalDateTime?) = apply { this.updateAt = updateAt }

        fun build() = Post(id, user, title, content, createdAt, updateAt, category, priceType, price, latitude, longitude, viewCount, postImages, postAvailabilities)
    }
}