package com.snackoverflow.toolgether.domain.postavailability.repository;

import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostAvailabilityRepository extends JpaRepository<PostAvailability, Long> {
}
