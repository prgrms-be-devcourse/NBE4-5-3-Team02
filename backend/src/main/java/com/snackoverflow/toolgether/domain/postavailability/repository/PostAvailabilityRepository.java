package com.snackoverflow.toolgether.domain.postavailability.repository;

import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAvailabilityRepository extends JpaRepository<PostAvailability, Long> {
    List<PostAvailability> findAllByPostId(Long postId);

    void deleteByPostId(Long postId);
}
