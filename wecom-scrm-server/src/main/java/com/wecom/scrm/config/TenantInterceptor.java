package com.wecom.scrm.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String corpId = request.getHeader("X-Corp-Id");
        
        // Skip for callback API as it handles it differently
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/wecom/callback/receive") || uri.startsWith("/api/auth/login")) {
            return true;
        }

        if (StringUtils.hasText(corpId)) {
            // Push Dynamic Datasource Context
            DynamicDataSourceContextHolder.push(corpId);
            
            // Switch WxCpService config via Manager
            WxCpServiceManager.setCurrentCorpId(corpId);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Always clean up to avoid thread local memory leaks
        DynamicDataSourceContextHolder.poll();
        DynamicDataSourceContextHolder.clear();
        WxCpServiceManager.clearCurrentCorpId();
    }
}
