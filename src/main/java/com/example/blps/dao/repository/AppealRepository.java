package com.example.blps.dao.repository;

import com.example.blps.dao.repository.model.Appeal;
import com.example.blps.dao.repository.model.VideoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, Long> {
    boolean existsByVideo(VideoInfo video);
    Optional<Appeal> findTopByVideoOrderByIdDesc(VideoInfo video);
}
