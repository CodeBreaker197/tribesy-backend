package com.tribesy.social.repository;

import com.tribesy.social.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p"
    )
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}