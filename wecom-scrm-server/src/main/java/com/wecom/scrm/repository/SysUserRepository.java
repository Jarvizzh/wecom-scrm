package com.wecom.scrm.repository;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wecom.scrm.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@DS("master")
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
}
