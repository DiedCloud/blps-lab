package com.example.blps.dao.repository;

import com.example.blps.dao.repository.model.MonetizationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonetizationInfoRepository extends JpaRepository<MonetizationInfo, Long> {
}
