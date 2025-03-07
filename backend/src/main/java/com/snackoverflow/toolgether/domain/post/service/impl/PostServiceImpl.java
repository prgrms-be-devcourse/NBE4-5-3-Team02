package com.snackoverflow.toolgether.domain.post.service.impl;

import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest;
import com.snackoverflow.toolgether.domain.post.entity.enums.RecurrenceDays;
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityRequest;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import com.snackoverflow.toolgether.domain.postavailability.repository.PostAvailabilityRepository;
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import com.snackoverflow.toolgether.domain.postimage.repository.PostImageRepository;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.BadRequestException;
import com.snackoverflow.toolgether.global.exception.NotFoundException;
import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostResponse;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.post.service.PostService;
import com.snackoverflow.toolgether.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostAvailabilityRepository postAvailabilityRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public PostResponse createPost(PostCreateRequest request) {
        // 임시로 user_id=1 사용 (실제 로직에서는 인증된 사용자 정보 가져오기)
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new NotFoundException("404-1", "사용자를 찾을 수 없습니다."));

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("400-1", "제목은 필수 입력값입니다.");
        }

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .priceType(request.getPriceType())
                .price(request.getPrice())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .viewCount(0)
                .build();

        postRepository.save(post);

        // 이미지 저장
        List<PostImage> images = request.getImages().stream()
                .map(image -> PostImage.builder()
                        .post(post)
                        .postImage(image)
                        .build())
                .toList();

        postImageRepository.saveAll(images);

        // 거래 가능 일정 저장
        savePostAvailabilities(post, request.getAvailabilities());

        return new PostResponse(post);
    }

    @Transactional
    @Override
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("404-1", "해당 게시물을 찾을 수 없습니다."));

        // 해당 게시물의 이미지 리스트 조회
        List<String> imageUrls = postImageRepository.findAllByPostId(postId)
                .stream()
                .map(PostImage::getPostImage) // PostImage 엔티티에서 이미지 경로 추출
                .toList();

        // 해당 게시물의 거래 가능 일정 조회
        List<PostAvailability> availabilities = postAvailabilityRepository.findAllByPostId(postId);

        return new PostResponse(post,imageUrls,availabilities);
    }

    @Transactional
    @Override
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("404-1", "해당 게시물을 찾을 수 없습니다."));

        // 연관된 이미지 삭제
        postImageRepository.deleteByPostId(postId);

        // 연관된 거래 가능 일정 삭제
        postAvailabilityRepository.deleteByPostId(postId);

        postRepository.delete(post);
    }

    @Transactional
    @Override
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        // 게시물 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("404-1", "해당 게시물을 찾을 수 없습니다."));

        // 기존 객체의 필드 값만 변경
        post.updatePost(
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getPriceType(),
                request.getPrice(),
                request.getLatitude(),
                request.getLongitude(),
                request.getViewCount()
        );

        // 기존 이미지 삭제 후 새로운 이미지 저장
        postImageRepository.deleteByPostId(postId);
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<PostImage> images = request.getImages().stream()
                    .map(image -> PostImage.builder()
                            .post(post)
                            .postImage(image)
                            .build())
                    .toList();
            postImageRepository.saveAll(images);
        }

        // 기존 거래 가능 일정 삭제 후 새로운 일정 저장
        postAvailabilityRepository.deleteByPostId(postId);
        if (request.getAvailabilities() != null && !request.getAvailabilities().isEmpty()) {
            savePostAvailabilities(post, request.getAvailabilities());
        }

        // 수정된 게시물 반환
        List<String> imageUrls = postImageRepository.findAllByPostId(postId)
                .stream()
                .map(PostImage::getPostImage)
                .toList();

        List<PostAvailability> availabilities = postAvailabilityRepository.findAllByPostId(postId);

        return new PostResponse(post, imageUrls, availabilities);
    }

    private void savePostAvailabilities(Post post, List<PostAvailabilityRequest> availabilities) {
        List<PostAvailability> postAvailabilities = availabilities.stream()
                .map(availability -> PostAvailability.builder()
                        .post(post)
                        .date(availability.getDate())
                        .recurrence_days(availability.getRecurrenceDays() != null ? availability.getRecurrenceDays() : RecurrenceDays.NONE.getCode())
                        .startTime(availability.getStartTime())
                        .endTime(availability.getEndTime())
                        .isRecurring(availability.isRecurring())
                        .build())
                .toList();
        postAvailabilityRepository.saveAll(postAvailabilities);
    }

    // 예약에 필요한 메서드
    @Transactional(readOnly = true)
    @Override
    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

}
