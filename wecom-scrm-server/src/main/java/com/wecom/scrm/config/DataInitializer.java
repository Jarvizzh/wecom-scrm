package com.wecom.scrm.config;

import com.wecom.scrm.entity.SysUser;
import com.wecom.scrm.repository.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserRepository sysUserRepository, PasswordEncoder passwordEncoder) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking for default admin user...");
        
        Optional<SysUser> adminUser = sysUserRepository.findByUsername("admin");
        
        if (!adminUser.isPresent()) {
            log.info("Default admin user not found. Initializing...");
            
            SysUser user = new SysUser();
            user.setUsername("admin");
            // Encode the password using the system's password encoder
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickname("Administrator");
            
            sysUserRepository.save(user);
            log.info("Successfully seeded default admin user (admin / 123456)");
        } else {
            log.info("Default admin user already exists.");
            
            // Optional: You can force a password reset here for testing if the user is stuck
            // SysUser user = adminUser.get();
            // user.setPassword(passwordEncoder.encode("123456"));
            // sysUserRepository.save(user);
        }
    }
}
