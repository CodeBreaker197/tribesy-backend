package com.tribesy.social.repository;

import com.tribesy.social.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p"
    )
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.author " +
                    "WHERE p.author.id IN :followingIds " +
                    "ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.author.id IN :followingIds"
    )
    Page<Post> findFeedByAuthorIds(@Param("followingIds") List<Long> followingIds, Pageable pageable);
}