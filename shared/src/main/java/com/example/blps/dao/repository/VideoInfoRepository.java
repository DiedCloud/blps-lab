package com.example.blps.dao.repository;

import com.example.blps.dao.repository.model.VideoInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoInfoRepository extends JpaRepository<VideoInfo, Long> {
    @Query("SELECT v FROM VideoInfo v WHERE v.lastAccessTime < :threshold")
    List<VideoInfo> findOldVideos(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query("DELETE FROM VideoInfo v WHERE v.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}
