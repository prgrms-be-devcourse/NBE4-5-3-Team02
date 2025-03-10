package com.snackoverflow.toolgether.domain.post.controller;

import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostResponse;
import com.snackoverflow.toolgether.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * 게시물 작성
     */
    //    @Operation(summary = "게시물 등록")
    @PostMapping
    public RsData<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request) {
        return new RsData<>(
                "201-1",
                "게시물이 성공적으로 등록되었습니다.",
                postService.createPost(request)
        );
    }


    /**
     * 게시물 상세조회
     */
//    @Operation(summary = "게시물 상세 조회")
    @GetMapping("/{postId}")
    public RsData<PostResponse> getPostDetail(@PathVariable Long postId) {
        return new RsData<>(
                "200-1",
                "게시물 조회 성공",
                postService.getPostById(postId)
        );
    }

    /**
     * 게시물 삭제
     */
//    @Operation(summary = "게시물 삭제")
    @DeleteMapping("/{postId}")
    public RsData<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return new RsData<>(
                "204-1",
                "게시물이 성공적으로 삭제되었습니다.",
                null
        );
    }

    /**
     * 게시물 수정
     */
    //    @Operation(summary = "게시물 수정")
    @PutMapping("/{postId}")
    public RsData<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateRequest request) {
        return new RsData<>(
                "200-2",
                "게시물이 성공적으로 수정되었습니다.",
                postService.updatePost(postId, request)
        );
    }

    /**
     * 게시물 검색 및 필터링 조회 API (페이징)
     */
    // @Operation(summary = "게시물 검색 및 필터링 조회 API")
    @GetMapping("/search")
    public RsData<Page<PostResponse>> searchPosts(@ModelAttribute PostSearchRequest request, Pageable pageable) {
        return new RsData<>("200-1",
                "게시물 검색 결과입니다.",
                postService.searchPosts(request, pageable)
        );
    }

}
