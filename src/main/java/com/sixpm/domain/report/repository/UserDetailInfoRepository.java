package com.sixpm.domain.report.repository;

import com.sixpm.domain.report.entity.UserDetailInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailInfoRepository extends JpaRepository<UserDetailInfo, Long> {
    Optional<UserDetailInfo> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

