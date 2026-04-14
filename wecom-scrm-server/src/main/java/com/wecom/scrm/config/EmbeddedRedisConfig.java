package com.wecom.scrm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;

@Slf4j
@Component
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        if (isPortInUse(redisPort)) {
            log.info("Redis is already running on port {}, skipping embedded Redis startup.", redisPort);
            return;
        }

        try {
            redisServer = new RedisServer(redisPort);
            redisServer.start();
            log.info("Embedded Redis started on port {}", redisPort);
        } catch (Exception e) {
            log.error("Failed to start Embedded Redis on port {}. Error: {}", redisPort, e.getMessage());
        }
    }

    private boolean isPortInUse(int port) {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
            log.info("Embedded Redis stopped");
        }
    }
}
