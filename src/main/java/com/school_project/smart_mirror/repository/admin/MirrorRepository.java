package com.school_project.smart_mirror.repository.admin;

import com.school_project.smart_mirror.domain.admin.Mirror;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorRepository extends JpaRepository<Mirror, Integer> {

}
