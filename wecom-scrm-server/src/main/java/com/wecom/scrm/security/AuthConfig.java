package com.wecom.scrm.security;

import com.wecom.scrm.entity.SysUser;
import com.wecom.scrm.repository.SysUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AuthConfig {

    @Bean
    public UserDetailsService userDetailsService(SysUserRepository sysUserRepository) {
        return username -> {
            SysUser sysUser = sysUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            if (sysUser.getStatus() != null && sysUser.getStatus() == 1) {
                throw new UsernameNotFoundException("User is banned: " + username);
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            if (sysUser.getIsSuperAdmin() != null && sysUser.getIsSuperAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
