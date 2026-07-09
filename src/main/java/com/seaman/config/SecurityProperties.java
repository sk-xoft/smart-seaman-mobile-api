package com.seaman.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "smart-seaman.security")
public class SecurityProperties {

    private final Cors cors = new Cors();
    private final PublicPaths publicPaths = new PublicPaths();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOriginPatterns = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class PublicPaths {
        private boolean exposeDocs = false;
        private boolean exposeActuator = false;
    }
}
