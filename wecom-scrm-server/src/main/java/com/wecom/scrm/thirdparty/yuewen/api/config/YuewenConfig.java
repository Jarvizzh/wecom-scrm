package com.wecom.scrm.thirdparty.yuewen.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Yuewen API credentials.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yuewen.api")
public class YuewenConfig {
    /**
     * Partner email registered on Yuewen Open Platform.
     */
    private String email;

    /**
     * App secret for signature generation.
     */
    private String appSecret;

    /**
     * API version (default is 1).
     */
    private Integer version = 1;

    /**
     * Base URL for Yuewen API.
     */
    private String baseUrl = "https://fxapi.yuewen.com";
}
