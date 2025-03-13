package com.snackoverflow.toolgether.domain.post.service.impl;

import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest;
import com.snackoverflow.toolgether.domain.post.entity.enums.RecurrenceDays;
import com.snackoverflow.toolgether.domain.post.repository.PostRepositoryCustom;
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
import com.snackoverflow.toolgether.global.util.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostRepositoryCustom postQueryRepository;
    private final PostImageRepository postImageRepository;
    private final PostAvailabilityRepository postAvailabilityRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    @Override
    public PostResponse createPost(User user,PostCreateRequest request, List<MultipartFile> images) {
        // 임시로 user_id=1 사용 (실제 로직에서는 인증된 사용자 정보 가져오기)
//        User user = userRepository.findById(1L)
//                .orElseThrow(() -> new NotFoundException("404-1", "사용자를 찾을 수 없습니다."));

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

        // S3 이미지 업로드 후 저장
        List<PostImage> postImages = images.stream()
                .map(file -> PostImage.builder()
                        .post(post)
                        .imageUrl(s3Service.upload(file, "post-images"))
                        .build())
                .collect(Collectors.toList());

        postImageRepository.saveAll(postImages);

        // 거래 가능 일정 저장
        savePostAvailabilities(post, request.getAvailabilities());

        return new PostResponse(post);
    }

    @Transactional
    @Override
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("404-1", "게시물을 찾을 수 없습니다."));

        // 조회수 증가
        post.incrementViewCount();

        // Set<String> 이미지 경로로 변환
        Set<String> imageUrls = post.getPostImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toSet());

        // PostAvailability Set으로 변환
        Set<PostAvailability> availabilities = new HashSet<>(post.getPostAvailabilities());

        return new PostResponse(post, imageUrls, availabilities);
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
                            .imageUrl(image)
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
                .map(PostImage::getImageUrl)
                .toList();

        List<PostAvailability> availabilities = postAvailabilityRepository.findAllByPostId(postId);

//        return new PostResponse(post, imageUrls, availabilities);
        return null;
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

    @Transactional
    @Override
    public Page<PostResponse> searchPosts(PostSearchRequest request, Pageable pageable) {
//        Page<Post> posts = postQueryRepository.searchPosts(request, pageable);
        return postQueryRepository.searchPosts(request, pageable);
    }

    // 예약에 필요한 메서드
    @Transactional(readOnly = true)
    @Override
    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

}
