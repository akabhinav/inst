package com.instagram.post.repository;

import com.instagram.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByUserIdAndIsArchivedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Post> findByUserIdInAndIsArchivedFalseOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    List<Post> findByIdInOrderByCreatedAtDesc(List<Long> postIds);

    long countByUserIdAndIsArchivedFalse(Long userId);

    @Query("SELECT p FROM Post p WHERE p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActivePosts(Pageable pageable);
}
