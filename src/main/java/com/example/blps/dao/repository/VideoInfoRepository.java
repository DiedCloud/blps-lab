package com.example.blps.dao.repository;

import com.example.blps.dao.repository.model.VideoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoInfoRepository extends JpaRepository<VideoInfo, Long> {
}
