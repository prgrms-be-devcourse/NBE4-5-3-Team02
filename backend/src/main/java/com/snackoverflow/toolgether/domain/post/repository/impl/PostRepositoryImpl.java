package com.snackoverflow.toolgether.domain.post.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.QPost;
import com.snackoverflow.toolgether.domain.post.repository.PostRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;

    @Override
    public Page<Post> searchPosts(PostSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        log.info("검색 키워드 : {}" , request.getKeyword());

        if (request.getKeyword() != null) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            booleanBuilder.or(post.title.containsIgnoreCase(request.getKeyword()));
            booleanBuilder.or(post.content.containsIgnoreCase(request.getKeyword()));

            builder.and(booleanBuilder);
        }

        log.info("조건이 있는지 확인 : {}", builder.hasValue());

        if (request.getCategory() != null) {
            builder.and(post.category.eq(request.getCategory()));
        }

        if (request.getPriceType() != null) {
            builder.and(post.priceType.eq(request.getPriceType()));
        }

        if (request.getMinPrice() != null) {
            // 최소 금액 이상
            builder.and(post.price.goe(request.getMinPrice()));
        }

        if (request.getMaxPrice() != null) {
            // 최대 금액 이하
            builder.and(post.price.loe(request.getMaxPrice()));
        }

        // 반경에 따른 필터링
        if (request.getLatitude() != null && request.getLongitude() != null && request.getDistance() != null) {
            double latitude = request.getLatitude(); // 사용자 위도
            double longitude = request.getLongitude(); // 사용자 경도
            double distanceInKm = request.getDistance(); // 1km, 3km, 5km 반경 선택

            // Haversine 공식을 활용 (위도,경도 사이의 거리 계산)
            NumberExpression<Double> distanceExpression = Expressions.numberTemplate(Double.class,
                    "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
                    latitude, post.latitude, post.longitude, longitude);

            builder.and(distanceExpression.loe(distanceInKm)); // 지정된 거리 반경 일때만 게시물만 조회
        }

        List<Post> results = queryFactory
                .selectFrom(post)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(results, pageable, () -> queryFactory
                .selectFrom(post)
                .where(builder)
                .fetch().size());
    }

}
