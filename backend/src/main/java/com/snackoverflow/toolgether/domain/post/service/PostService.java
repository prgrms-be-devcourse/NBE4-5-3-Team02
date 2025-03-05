package com.snackoverflow.toolgether.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;

	@Transactional(readOnly = true)
	public Post findPostById(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> new RuntimeException("Post not found"));
	}
}