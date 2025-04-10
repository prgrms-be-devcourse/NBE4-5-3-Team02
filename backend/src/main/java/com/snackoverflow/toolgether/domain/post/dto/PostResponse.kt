package com.snackoverflow.toolgether.domain.post.dto

import com.snackoverflow.toolgether.domain.post.entity.Post
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityResponse
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class PostResponse @JvmOverloads constructor(
    val post: Post,
    val images: Set<String> = setOf(),
    availabilities: Set<PostAvailability> = setOf<PostAvailability>()
) {
    var id: Long? = null

    // this.id = post.getId();
    val userid: Long? = post.user?.id
    val nickname: String? = post.user?.nickname
    val title = post.title
    val content = post.content
    val category = post.category.name
    val priceType = post.priceType.name
    val price = post.price
    val latitude = post.latitude
    val longitude = post.longitude
    val createdAt: String? // 날짜 포맷 적용
    val viewCount = post.viewCount
    val availabilities: Set<PostAvailabilityResponse>

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    //빈 Set을 기본값으로 사용하여 다른 생성자 호출
    init {
        this.createdAt = if (post.createdAt != null) post.createdAt?.format(FORMATTER) else null

        //images와 availabilities를 Set으로 유지
        this.availabilities = availabilities.stream()
            .map { avail -> PostAvailabilityResponse(avail, FORMATTER) }
            .collect(Collectors.toSet())
    }
}
