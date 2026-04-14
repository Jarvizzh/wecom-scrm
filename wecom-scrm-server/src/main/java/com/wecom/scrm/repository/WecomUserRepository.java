package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WecomUserRepository extends JpaRepository<WecomUser, Long> {
    Optional<WecomUser> findByUserid(String userid);
    
    List<WecomUser> findByUseridIn(List<String> userids);
    
    Page<WecomUser> findByNameContainingOrUseridContaining(String name, String userid, Pageable pageable);
    
    @Query(value = "SELECT * FROM wecom_user WHERE JSON_CONTAINS(department_ids, CAST(:departmentIdStr AS JSON))", 
           countQuery = "SELECT count(*) FROM wecom_user WHERE JSON_CONTAINS(department_ids, CAST(:departmentIdStr AS JSON))", 
           nativeQuery = true)
    Page<WecomUser> findByDepartmentId(
            @Param("departmentIdStr") String departmentIdStr, 
            Pageable pageable);
            
    @Query(value = "SELECT * FROM wecom_user WHERE JSON_CONTAINS(department_ids, CAST(:departmentIdStr AS JSON)) AND (name LIKE CONCAT('%', :keyword, '%') OR userid LIKE CONCAT('%', :keyword, '%'))", 
           countQuery = "SELECT count(*) FROM wecom_user WHERE JSON_CONTAINS(department_ids, CAST(:departmentIdStr AS JSON)) AND (name LIKE CONCAT('%', :keyword, '%') OR userid LIKE CONCAT('%', :keyword, '%'))", 
           nativeQuery = true)
    Page<WecomUser> findByDepartmentIdAndKeyword(
            @Param("departmentIdStr") String departmentIdStr, 
            @Param("keyword") String keyword, 
            Pageable pageable);
}
