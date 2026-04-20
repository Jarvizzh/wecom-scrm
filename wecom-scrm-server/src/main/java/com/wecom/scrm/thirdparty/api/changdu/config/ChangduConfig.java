package com.wecom.scrm.thirdparty.api.changdu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Changdu Content Distribution Platform.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "changdu.api")
public class ChangduConfig {
    /**
     * Base URL for Changdu API.
     */
    private String baseUrl = "https://openapi.changdupingtai.com";
    
    /**
     * Default distributor identifier (for obtaining all sub-distributors).
     */
    private String distributorId;
    
    /**
     * Secret key for signing.
     */
    private String secretKey;
}
