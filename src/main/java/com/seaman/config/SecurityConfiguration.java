package com.seaman.config;

import com.seaman.exception.CustomAuthenticationEntryPoint;
import com.seaman.filter.TokenFilterConfiguerer;
import com.seaman.service.JwtTokenService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtTokenService jwtTokenService;

    private final MessageCodeService messageCodeService;

    private final String[] PUBLIC = {
            "/actuator/**",
            "/swagger-ui.html/**",
            "/swagger-ui/**",
            "/smart-seaman-swagger/**",
            "/v1/login",
            "/v1/register",
            "/v1/refresh-token",
            "/v1/activate-user/**",
            "/v1/reset-password",
            "/v1/activate-forgot-password",
            "/v1/profile/active",

            // Master data
            "/v1/master",
            "/.well-known/assetlinks.json",
            "/.well-known/apple-app-site-association"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors(config -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowCredentials(true);
                    cors.setAllowedOriginPatterns(Collections.singletonList("http://*"));
                    cors.addAllowedHeader("*");
                    cors.addAllowedMethod("GET");
                    cors.addAllowedMethod("POST");
                    cors.addAllowedMethod("PUT");
                    cors.addAllowedMethod("DELETE");
                    cors.addAllowedMethod("OPTIONS");

                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", cors);

                    config.configurationSource(source);
                }).csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests().antMatchers(PUBLIC).permitAll()
                .anyRequest().authenticated()
                .and().apply(new TokenFilterConfiguerer(jwtTokenService, messageCodeService));

        // Handle Exception.
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());

        http
                .headers()
                .addHeaderWriter(new StaticHeadersWriter("X-Frame-Options", "SAMEORIGIN"))
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "script-src 'self'"))
                .addHeaderWriter(new StaticHeadersWriter("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**
        auth.inMemoryAuthentication()
                .withUser("smartseaman")
                .password(passwordEncoder().encode("P@55w0rd"))
                .authorities("ADMIN");
         */
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return new CustomAuthenticationEntryPoint();
    }
}

