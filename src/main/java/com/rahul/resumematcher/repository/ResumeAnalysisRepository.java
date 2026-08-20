package com.rahul.resumematcher.repository;

import com.rahul.resumematcher.entity.ResumeAnalysis;
import com.rahul.resumematcher.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findByUserOrderByCreatedAtDesc(User user);
    Optional<ResumeAnalysis> findByIdAndUser(Long id, User user);
}
