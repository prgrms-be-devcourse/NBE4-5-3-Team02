package com.snackoverflow.toolgether.domain.post.service.impl

import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest
import com.snackoverflow.toolgether.domain.post.dto.PostResponse
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest
import com.snackoverflow.toolgether.domain.post.entity.Post
import com.snackoverflow.toolgether.domain.post.entity.Post.Companion.builder
import com.snackoverflow.toolgether.domain.post.entity.enums.RecurrenceDays
import com.snackoverflow.toolgether.domain.post.repository.PostRepository
import com.snackoverflow.toolgether.domain.post.repository.PostRepositoryCustom
import com.snackoverflow.toolgether.domain.post.service.PostService
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityRequest
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability
import com.snackoverflow.toolgether.domain.postavailability.repository.PostAvailabilityRepository
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage
import com.snackoverflow.toolgether.domain.postimage.repository.PostImageRepository
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.global.exception.BadRequestException
import com.snackoverflow.toolgether.global.exception.NotFoundException
import com.snackoverflow.toolgether.global.util.s3.S3Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.stream.Collectors

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val postQueryRepository: PostRepositoryCustom,
    private val postImageRepository: PostImageRepository,
    private val postAvailabilityRepository: PostAvailabilityRepository,
    private val s3Service: S3Service,
) : PostService {


    @Transactional
    override fun createPost(user: User, request: PostCreateRequest, images: List<MultipartFile>): PostResponse {
        if (request.title == null || request.title.isBlank()) {
            throw BadRequestException("400-1", "제목은 필수 입력값입니다.")
        }

        val post = builder()
            .user(user)
            .title(request.title)
            .content(request.content)
            .category(request.category)
            .priceType(request.priceType)
            .price(request.price)
            .latitude(request.latitude)
            .longitude(request.longitude)
            .viewCount(0)
            .build()

        postRepository.save(post)

        // S3 이미지 업로드 후 저장
        val postImages = images.map { file: MultipartFile? ->
                PostImage(
                    post = post,
                    imageUrl = s3Service.upload(file!!, "post-images")
                )
            }.toList()

        postImageRepository.saveAll(postImages)

        // 거래 가능 일정 저장
        savePostAvailabilities(post, request.availabilities)

        return PostResponse(post)
    }

    @Transactional
    override fun getPostById(postId: Long): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow {
                NotFoundException(
                    "404-1",
                    "게시물을 찾을 수 없습니다."
                )
            }

        // 조회수 증가
        post.incrementViewCount()

        // Set<String> 이미지 경로로 변환
        val imageUrls = post.postImages
            .map { obj: PostImage -> obj.imageUrl }.toSet()

        // PostAvailability Set으로 변환
        val availabilities: Set<PostAvailability> = HashSet(post.postAvailabilities)

        return PostResponse(post = post, images = imageUrls, availabilities = availabilities)
    }

    @Transactional
    override fun deletePost(postId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow {
                NotFoundException(
                    "404-1",
                    "해당 게시물을 찾을 수 없습니다."
                )
            }

        // 연관된 이미지 삭제
        postImageRepository.deleteByPostId(postId)

        // 연관된 거래 가능 일정 삭제
        postAvailabilityRepository.deleteByPostId(postId)

        postRepository.delete(post)
    }

    @Transactional
    override fun updatePost(postId: Long, request: PostUpdateRequest): PostResponse? {
        // 게시물 조회
        val post = postRepository.findById(postId)
            .orElseThrow {
                NotFoundException(
                    "404-1",
                    "해당 게시물을 찾을 수 없습니다."
                )
            }

        // 기존 객체의 필드 값만 변경
        post.updatePost(
            request.title,
            request.content,
            request.category,
            request.priceType,
            request.price,
            request.latitude,
            request.longitude,
            request.viewCount
        )

        // 기존 이미지 삭제 후 새로운 이미지 저장
        postImageRepository.deleteByPostId(postId)
        if (request.images != null && !request.images.isEmpty()) {
            val images = request.images
                .map { image: String ->
                    PostImage(
                        post = post,
                        imageUrl = image
                    )
                }
                .toList()
            postImageRepository.saveAll(images)
        }

        // 기존 거래 가능 일정 삭제 후 새로운 일정 저장
        postAvailabilityRepository.deleteByPostId(postId)
        if (request.availabilities != null && !request.availabilities.isEmpty()) {
            savePostAvailabilities(post, request.availabilities)
        }

        // 수정된 게시물 반환
        val imageUrls = postImageRepository.findAllByPostId(postId)
            .map { obj: PostImage -> obj.imageUrl }
            .toList()

        val availabilities = postAvailabilityRepository.findAllByPostId(postId)

        //        return new PostResponse(post, imageUrls, availabilities);
        return null
    }

    private fun savePostAvailabilities(post: Post, availabilities: List<PostAvailabilityRequest>) {
        val postAvailabilities = availabilities
            .map { availability: PostAvailabilityRequest ->
                PostAvailability(
                    post = post,
                    date = availability.date,
                    recurrence_days = if (availability.recurrenceDays != null) availability.recurrenceDays else RecurrenceDays.NONE.getCode(),
                    startTime = availability.startTime,
                    endTime = availability.endTime,
                    isRecurring = availability.isRecurring
                )
            }
            .toList()
        postAvailabilityRepository!!.saveAll(postAvailabilities)
    }

    @Transactional
    override fun searchPosts(request: PostSearchRequest, pageable: Pageable): Page<PostResponse> {
//        Page<Post> posts = postQueryRepository.searchPosts(request, pageable);
        return null
    }

    // 예약에 필요한 메서드
    @Transactional(readOnly = true)
    override fun findPostById(postId: Long): Post {
        return postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }
    }
}
