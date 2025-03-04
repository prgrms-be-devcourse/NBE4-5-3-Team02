package com.snackoverflow.toolgether.domain.Post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.snackoverflow.toolgether.domain.Post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
