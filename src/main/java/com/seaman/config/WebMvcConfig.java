package com.seaman.config;

import com.seaman.interceptor.APIInterceptor;
import com.seaman.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

        private final APIInterceptor apiInterceptor;

        private final AuthInterceptor authInterceptor;

        private final String[] NOT_VALIDATE_AUTH = {
                        "/actuator/**",
                        "/swagger-ui.html",
                        "/swagger-ui.html/**",
                        "/swagger-ui/**",
                        "/smart-seaman-swagger",
                        "/smart-seaman-swagger/**",
                        "/v3/api-docs/**",
                        "/v1/login",
                        "/v1/register",
                        "/v1/refresh-token",
                        "/v1/activate-user",
                        "/v1/reset-password",
                        "/v1/activate-forgot-password",
                        "/v1/profile/active",
                        "/v1/payments/omise/webhook",
                        "/forgot-password/**",
                        "/confirm-register/**",

                        // Master data
                        "/v1/master"
        };

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(apiInterceptor)
                                .excludePathPatterns(
                                                "/swagger-ui.html",
                                                "/swagger-ui.html/**",
                                                "/swagger-ui/**",
                                                "/smart-seaman-swagger",
                                                "/smart-seaman-swagger/**")
                                .addPathPatterns("/v1/**")
                                .order(1);

                registry.addInterceptor(authInterceptor)
                                .excludePathPatterns(NOT_VALIDATE_AUTH).addPathPatterns("/v1/**")
                                .order(2);

        }

}
