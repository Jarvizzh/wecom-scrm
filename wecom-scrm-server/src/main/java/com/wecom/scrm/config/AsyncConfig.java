package com.wecom.scrm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "syncExecutor")
    public Executor syncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("syncExecutor-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return new TenantThrottlingExecutor(executor, 10);
    }

    @Bean(name = "syncCustomersExecutor")
    public Executor syncCustomersExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(50000);
        executor.setThreadNamePrefix("syncCustomersExecutor-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return new TenantThrottlingExecutor(executor, 5);
    }

    @Bean(name = "mpSyncExecutor")
    public Executor mpSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("mpSyncExecutor-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return new TenantThrottlingExecutor(executor, 5);
    }

    @Bean(name = "thirdPartySyncExecutor")
    public Executor thirdPartySyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(18);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("thirdPartySyncExecutor-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return new TenantThrottlingExecutor(executor, 6);
    }

    @Bean(name = "customerMessageExecutor")
    public Executor customerMessageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("msg-executor-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return new TenantThrottlingExecutor(executor, 5);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async error in {} with params: {}", method.getName(), params, throwable);
        };
    }

    /**
     * Wrapper executor that enforces per-tenant concurrency limits.
     * Uses WxCpServiceManager to identify the current tenant context.
     */
    public static class TenantThrottlingExecutor implements Executor {
        private final Executor delegate;
        private final int maxConcurrentPerTenant;
        private final Map<String, Semaphore> tenantSemaphores = new ConcurrentHashMap<>();

        public TenantThrottlingExecutor(Executor delegate, int maxConcurrentPerTenant) {
            this.delegate = delegate;
            this.maxConcurrentPerTenant = maxConcurrentPerTenant;
        }

        @Override
        public void execute(Runnable task) {
            String corpId = WxCpServiceManager.getCurrentCorpId();
            if (corpId == null) {
                delegate.execute(task);
                return;
            }

            // Get or create a semaphore for this tenant
            Semaphore semaphore = tenantSemaphores.computeIfAbsent(corpId, k -> new Semaphore(maxConcurrentPerTenant));

            try {
                // Block the calling thread if the tenant has already reached its concurrency
                // limit.
                // This provides natural backpressure and ensures the thread pool threads are
                // not
                // wasted waiting for a semaphore.
                semaphore.acquire();
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for tenant sync permit for corp: {}", corpId);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Tenant sync execution interrupted", e);
            }

            try {
                delegate.execute(() -> {
                    try {
                        task.run();
                    } finally {
                        semaphore.release();
                    }
                });
            } catch (Exception e) {
                // If submission to the delegate fails (e.g., pool full), release the permit
                // immediately
                semaphore.release();
                throw e;
            }
        }
    }
}
