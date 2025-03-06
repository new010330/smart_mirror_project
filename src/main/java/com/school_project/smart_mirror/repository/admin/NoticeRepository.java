package com.school_project.smart_mirror.repository.admin;

import com.school_project.smart_mirror.domain.admin.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Integer> {

//    List<Notice> findByEndDateLessThanEqual(LocalDate date);
    @Query("SELECT n FROM Notice n WHERE n.end_date <= :currentDate ORDER BY n.start_date ASC")
    List<Notice> findAllByNotifications(@Param("currentDate") LocalDate currentDate);
}
