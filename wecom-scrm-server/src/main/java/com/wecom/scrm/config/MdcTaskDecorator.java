package com.wecom.scrm.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * Task Decorator to propagate context from calling thread to async thread.
 * This includes:
 * - MDC (Logging context)
 * - Dynamic Data Source context (Tenant database)
 * - WxCpServiceManager corpId (Tenant enterprise ID)
 * - SecurityContext (Spring Security context)
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture context from the caller thread
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String dsKey = DynamicDataSourceContextHolder.peek();
        String corpId = WxCpServiceManager.getCurrentCorpId();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        return () -> {
            try {
                // Set the context in the new thread
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                if (dsKey != null) {
                    DynamicDataSourceContextHolder.push(dsKey);
                }
                if (corpId != null) {
                    WxCpServiceManager.setCurrentCorpId(corpId);
                }
                if (securityContext != null) {
                    SecurityContextHolder.setContext(securityContext);
                }
                
                runnable.run();

            } finally {
                // Clear the context after execution to avoid thread pollution
                MDC.clear();
                if (dsKey != null) {
                    DynamicDataSourceContextHolder.poll();
                }
                WxCpServiceManager.clearCurrentCorpId();
                SecurityContextHolder.clearContext();
            }
        };
    }
}
