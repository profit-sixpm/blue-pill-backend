package com.sixpm.domain.announcement.repository;

import com.sixpm.domain.announcement.entity.AnnouncementCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementCriteriaRepository extends JpaRepository<AnnouncementCriteria, Long> {
    Optional<AnnouncementCriteria> findByAnnouncementId(Long announcementId);
}
