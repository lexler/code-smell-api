package com.codesmell.api.auth;

import com.codesmell.api.CodeSmellApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeyStartupCheckTest {

    @Test
    void allowsMissingApiKeyLocally() {
        assertThatCode(() -> {
            try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
                context.isRunning();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingApiKeyInDeployedProfile() {
        assertThatThrownBy(() -> {
            try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("deployed")
                .run()) {
                context.isRunning();
            }
        }).hasMessage("code-smell.api-key is required in the deployed profile");
    }

    @Test
    void allowsConfiguredApiKeyInDeployedProfile() {
        assertThatCode(() -> {
            try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("deployed")
                .properties("code-smell.api-key=secret")
                .run()) {
                context.isRunning();
            }
        }).doesNotThrowAnyException();
    }
}
