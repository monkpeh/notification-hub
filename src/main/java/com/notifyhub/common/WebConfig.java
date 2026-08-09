package com.notifyhub.common;

import com.notifyhub.security.RateLimitInterceptor;
import com.notifyhub.security.RoleCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RoleCheckInterceptor roleCheckInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(RoleCheckInterceptor roleCheckInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        this.roleCheckInterceptor = roleCheckInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleCheckInterceptor);
        registry.addInterceptor(rateLimitInterceptor);
    }
}
