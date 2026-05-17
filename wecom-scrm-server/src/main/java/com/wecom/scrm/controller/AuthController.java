package com.wecom.scrm.controller;

import com.wecom.scrm.entity.SysUser;
import com.wecom.scrm.repository.SysUserRepository;
import com.wecom.scrm.security.JwtUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserRepository sysUserRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, SysUserRepository sysUserRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.sysUserRepository = sysUserRepository;
    }

    @GetMapping("/secret/{secretKey}")
    public ResponseEntity<?> secret(@PathVariable String secretKey) {
        log.info("Login attempt for user: {}", secretKey);
        try {
            //todo
            if (secretKey.equals("jarvis") || secretKey.equals("youji1119")) {
                return ResponseEntity.ok("secretKey验证通过");
            } else {
                log.warn("Login failed: Invalid credentials for secretKey {}", secretKey);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("秘钥错误"));
            }
        } catch (Exception e) {
            log.error("Unexpected error during login for secretKey {}", secretKey, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("系统繁忙，请稍后再试"));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            String token = jwtUtils.generateToken(authentication.getName());
            
            SysUser user = sysUserRepository.findByUsername(authentication.getName()).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", authentication.getName());
            if (user != null) {
                response.put("nickname", user.getNickname());
                response.put("avatar", user.getAvatar());
                response.put("isSuperAdmin", user.getIsSuperAdmin());
                response.put("permissions", user.getPermissions());
            }

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid credentials for user {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("用户名或密码错误"));
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("身份验证失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("系统繁忙，请稍后再试"));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return error;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
