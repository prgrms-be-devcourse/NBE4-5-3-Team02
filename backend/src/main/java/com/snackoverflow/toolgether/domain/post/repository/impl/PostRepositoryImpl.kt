package com.snackoverflow.toolgether.domain.post.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.snackoverflow.toolgether.domain.post.dto.PostResponse
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest
import com.snackoverflow.toolgether.domain.post.entity.Post
import com.snackoverflow.toolgether.domain.post.entity.QPost
import com.snackoverflow.toolgether.domain.post.entity.QPost.post
import com.snackoverflow.toolgether.domain.post.repository.PostRepositoryCustom
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability
import com.snackoverflow.toolgether.domain.postavailability.entity.QPostAvailability
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage
import com.snackoverflow.toolgether.domain.postimage.entity.QPostImage
import com.snackoverflow.toolgether.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.util.stream.Collectors

@Repository
class PostRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val userRepository: UserRepository
) : PostRepositoryCustom {
    private val post: QPost = QPost.post
    private val postImage: QPostImage = QPostImage.postImage
    private val postAvailability: QPostAvailability = QPostAvailability.postAvailability

    companion object {
        private val log = LoggerFactory.getLogger(PostRepositoryImpl::class.java)
    }

    override fun searchPosts(request: PostSearchRequest, latitude: Double, longitude: Double, pageable: Pageable): Page<PostResponse> {
        val builder = BooleanBuilder()

        log.info("검색 키워드: ${request.keyword}")

        if (request.keyword != null && !request.keyword.isBlank()) {
            val keywordBuilder = BooleanBuilder()
            keywordBuilder.or(post.title.containsIgnoreCase(request.keyword))
            keywordBuilder.or(post.content.containsIgnoreCase(request.keyword))
            builder.and(keywordBuilder)
        }

        if (request.category != null) {
            builder.and(post.category.eq(request.category))
        }

        if (request.priceType != null) {
            builder.and(post.priceType.eq(request.priceType))
        }

        if (request.minPrice != null) {
            builder.and(post.price.goe(request.minPrice))
        }

        if (request.maxPrice != null) {
            builder.and(post.price.loe(request.maxPrice))
        }

        if (latitude != null && longitude != null && request.distance != null) {
//        if (request.latitude != null && request.longitude != null && request.distance != null) {
//            val latitude = request.latitude
//            val longitude = request.longitude
            val distanceInKm = request.distance

            log.info("lat = $latitude, lon = $longitude, distance = $distanceInKm")

//            checkDistance(latitude, longitude, builder, distanceInKm)
//        } else {
//            // 사용자 ID를 기반으로 데이터베이스에서 사용자 정보를 조회
//            val user = userRepository!!.findById(
//                request.userId!!.toLong()
//            )
//                .orElseThrow { IllegalArgumentException("사용자 정보를 찾을 수 없습니다.") }
//
//            // 사용자 정보에서 위도와 경도를 가져옴
//            val latitude = user.getLatitude()
//            val longitude = user.getLongitude()
//            val distanceInKm = request.distance
//
//            log.info(
//                "Request에서 lat/lon이 없으므로 사용자 데이터베이스에서 가져옵니다: lat = $latitude, lon = $longitude"
//            )

            checkDistance(latitude, longitude, builder, distanceInKm)
        }

        val results = queryFactory
            .selectFrom(post)
            .leftJoin(post.postImages, postImage).fetchJoin()
            .leftJoin(post.postAvailabilities, postAvailability).fetchJoin()
            .where(builder)
            .distinct()
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()


        val postResponses = results.stream()
            .map<PostResponse> { p: Post ->
                val imageUrls = if (p.postImages != null)
                    p.postImages.stream()
                        .map<String> { img: PostImage ->
                            log.info("게시물 ID: ${p.id}, 이미지 ID: ${img.id}, URL: ${img.imageUrl}")
                            img.imageUrl
                        }
                        .collect(Collectors.toSet<String>())
                else
                    HashSet<String>()
                val availabilities: Set<PostAvailability> = if (p.postAvailabilities != null)
                    HashSet(p.postAvailabilities)
                else
                    HashSet()

                log.info(
                    "PostResponse 생성 전 - 게시물 ID: ${p.id}, 이미지 개수: ${imageUrls.size}, 스케줄 개수: ${availabilities.size}"
                )
                PostResponse(p, imageUrls, availabilities)
            }
            .collect(Collectors.toList<PostResponse>())

        return PageableExecutionUtils.getPage(postResponses, pageable) {
            results.size.toLong()
                .toLong()
        }
    }

    private fun checkDistance(latitude: Double, longitude: Double, builder: BooleanBuilder, distanceInKm: Double) {
        val distanceExpression: NumberExpression<Double> = Expressions.numberTemplate(
            Double::class.java,
            "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
            latitude, post.latitude, post.longitude, longitude
        )

        builder.and(distanceExpression.loe(distanceInKm))
    }
}
