package com.instagram.post.repository;

import com.instagram.post.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndParentCommentIdIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId, Pageable pageable);

    long countByPostId(Long postId);

    long countByParentCommentId(Long parentCommentId);
}
