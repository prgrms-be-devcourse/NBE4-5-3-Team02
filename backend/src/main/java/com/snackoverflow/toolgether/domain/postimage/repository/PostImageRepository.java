package com.snackoverflow.toolgether.domain.postimage.repository;

import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostId(Long postId);
}
