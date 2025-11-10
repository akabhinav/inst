package com.instagram.user.repository;

import com.instagram.user.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId")
    Page<Long> findFollowingIds(Long userId, Pageable pageable);

    @Query("SELECT f.followerId FROM Follow f WHERE f.followingId = :userId")
    Page<Long> findFollowerIds(Long userId, Pageable pageable);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId")
    List<Long> findAllFollowingIds(Long userId);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);
}
