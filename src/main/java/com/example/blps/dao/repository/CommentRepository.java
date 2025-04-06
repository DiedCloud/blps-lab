package com.example.blps.dao.repository;

import com.example.blps.dao.repository.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
