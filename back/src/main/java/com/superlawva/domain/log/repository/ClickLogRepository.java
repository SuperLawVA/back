package com.superlawva.domain.log.repository;

import com.superlawva.domain.log.entity.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {
}