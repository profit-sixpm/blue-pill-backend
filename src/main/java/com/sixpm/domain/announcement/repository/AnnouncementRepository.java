package com.sixpm.domain.announcement.repository;

import com.sixpm.domain.announcement.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * 주택관리번호와 공고번호로 청약공고 조회
     */
    Optional<Announcement> findByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    /**
     * 수집일자로 청약공고 목록 조회
     */
    List<Announcement> findByFetchDate(String fetchDate);

    /**
     * 접수 기간으로 청약공고 목록 조회 (접수 시작일 >= 시작일, 접수 종료일 <= 종료일)
     */
    List<Announcement> findByRceptBgndeBetween(String startDate, String endDate);

    /**
     * 모집지역코드로 청약공고 목록 조회
     */
    List<Announcement> findBySubscrptAreaCode(String subscrptAreaCode);

    /**
     * 주택명으로 검색 (LIKE)
     */
    List<Announcement> findByHouseNmContaining(String keyword);

    /**
     * 주택관리번호와 공고번호로 존재 여부 확인
     */
    boolean existsByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    /**
     * 지역코드로 필터링하여 페이징 조회
     */
    Page<Announcement> findBySubscrptAreaCode(String subscrptAreaCode, Pageable pageable);
}

