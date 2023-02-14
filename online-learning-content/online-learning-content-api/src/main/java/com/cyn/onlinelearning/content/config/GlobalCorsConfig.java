package com.cyn.onlinelearning.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 21:25
 */
@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cors = new CorsConfiguration();
        // 允许白名单域名进行跨域调用
        cors.addAllowedOrigin("*");
        // 放行全部原始信息头
        cors.addAllowedHeader("*");
        // 允许跨域发送cookie
        cors.setAllowCredentials(true);
        // 允许所有请求方法调用
        cors.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 拦截所有请求
        source.registerCorsConfiguration("/**", cors);
        return new CorsFilter(source);
    }
}
