package com.school_project.smart_mirror.repository.admin;

import com.school_project.smart_mirror.domain.admin.Mirror;
import com.school_project.smart_mirror.domain.admin.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Integer> {

}
