package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WecomDepartmentRepository extends JpaRepository<WecomDepartment, Long> {
    Optional<WecomDepartment> findByDepartmentId(Long departmentId);
}
