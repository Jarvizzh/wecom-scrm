package com.wecom.scrm.controller;

import com.wecom.scrm.entity.SysUser;
import com.wecom.scrm.service.SysUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/users")
@PreAuthorize("hasRole('ADMIN')")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public List<SysUser> list() {
        return sysUserService.findAll();
    }

    @PostMapping
    public SysUser create(@RequestBody SysUser user) {
        return sysUserService.create(user);
    }

    @PutMapping
    public SysUser update(@RequestBody SysUser user) {
        return sysUserService.findById(user.getId()).map(existing -> {
            existing.setNickname(user.getNickname());
            existing.setAvatar(user.getAvatar());
            existing.setIsSuperAdmin(user.getIsSuperAdmin());
            existing.setPermissions(user.getPermissions());
            existing.setStatus(user.getStatus());
            return sysUserService.update(existing);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        sysUserService.updatePassword(id, request.getPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return ResponseEntity.ok().build();
    }

    public static class PasswordRequest {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
