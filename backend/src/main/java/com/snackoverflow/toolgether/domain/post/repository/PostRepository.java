package com.snackoverflow.toolgether.domain.post.repository;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
