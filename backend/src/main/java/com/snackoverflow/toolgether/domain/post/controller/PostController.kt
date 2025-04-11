package com.snackoverflow.toolgether.domain.post.controller

import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest
import com.snackoverflow.toolgether.domain.post.dto.PostResponse
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest
import com.snackoverflow.toolgether.domain.post.service.PostService
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.domain.user.service.UserService
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.exception.NotFoundException
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/posts")
class PostController(
    val postService: PostService,
    val userService: UserService
) {
    /**
     * 게시물 작성
     */
    //    @Operation(summary = "게시물 등록")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createPost(
        @Login customUserDetails: CustomUserDetails,
        @RequestPart("request") request: @Valid PostCreateRequest,
        @RequestPart("images") images: List<MultipartFile>
    ): RsData<PostResponse> {
        val userId = customUserDetails.userId
        val user: User = userService.findByUserId(userId) ?: throw NotFoundException(
            "404-1",
            "사용자를 찾을 수 없습니다."
        )

        return RsData(
            "201-1",
            "게시물이 성공적으로 등록되었습니다.",
            postService.createPost(user, request, images)
        )
    }

    /**
     * 게시물 상세조회
     */
    //    @Operation(summary = "게시물 상세 조회")
    @GetMapping("/{postId}")
    fun getPostDetail(@PathVariable postId: Long): RsData<PostResponse> {
        return RsData(
            "200-1",
            "게시물 조회 성공",
            postService.getPostById(postId)
        )
    }

    /**
     * 게시물 삭제
     */
    //    @Operation(summary = "게시물 삭제")
    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long): RsData<Void> {
        postService.deletePost(postId)
        return RsData(
            "204-1",
            "게시물이 성공적으로 삭제되었습니다.",
            null
        )
    }

    /**
     * 게시물 수정
     */
    //    @Operation(summary = "게시물 수정")
    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestBody request: @Valid PostUpdateRequest
    ): RsData<PostResponse> {
        return RsData(
            "200-2",
            "게시물이 성공적으로 수정되었습니다.",
            postService.updatePost(postId, request)
        )
    }

    /**
     * 게시물 검색 및 필터링 조회 API (페이징)
     */
    // @Operation(summary = "게시물 검색 및 필터링 조회 API")
    @GetMapping("/search")
    fun searchPosts(@ModelAttribute request: PostSearchRequest, pageable: Pageable): RsData<Page<PostResponse>> {
        return RsData(
            "200-1",
            "게시물 검색 결과입니다.",
            postService.searchPosts(request, pageable)
        )
    }
}
