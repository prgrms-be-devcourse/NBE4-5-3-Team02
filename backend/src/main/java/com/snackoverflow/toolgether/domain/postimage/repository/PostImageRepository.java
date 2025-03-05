package com.snackoverflow.toolgether.domain.postimage.repository;

import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

}
