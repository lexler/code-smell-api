package com.codesmell.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ApiKeyStartupCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyStartupCheck.class);

    private final Environment environment;
    private final String apiKey;

    public ApiKeyStartupCheck(Environment environment, @Value("${code-smell.api-key:}") String apiKey) {
        this.environment = environment;
        this.apiKey = apiKey;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (!apiKey.isBlank()) {
            return;
        }

        if (isDeployed()) {
            throw new IllegalStateException("code-smell.api-key is required in the deployed profile");
        }

        log.warn("WARNING: no API key configured - endpoint is open");
    }

    private boolean isDeployed() {
        return Arrays.asList(environment.getActiveProfiles()).contains("deployed");
    }
}
