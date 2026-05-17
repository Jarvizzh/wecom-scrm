package com.wecom.scrm.service;

import com.wecom.scrm.entity.SysUser;
import com.wecom.scrm.repository.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SysUserService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserRepository sysUserRepository, PasswordEncoder passwordEncoder) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SysUser> findAll() {
        return sysUserRepository.findAll();
    }

    public Optional<SysUser> findById(Long id) {
        return sysUserRepository.findById(id);
    }

    public Optional<SysUser> findByUsername(String username) {
        return sysUserRepository.findByUsername(username);
    }

    @Transactional
    public SysUser create(SysUser user) {
        log.info("Creating new system user: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return sysUserRepository.save(user);
    }

    @Transactional
    public SysUser update(SysUser user) {
        log.info("Updating system user: {}", user.getUsername());
        return sysUserRepository.save(user);
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        log.info("Updating password for user id: {}", id);
        sysUserRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            sysUserRepository.save(user);
        });
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting system user id: {}", id);
        sysUserRepository.deleteById(id);
    }
}
