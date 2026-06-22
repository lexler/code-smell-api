package com.codesmell.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class CodeSmellApiApplicationTest {

    @Test
    void startsApplicationContext() {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.NONE)
            .run()) {
            assertThat(context.isRunning()).isTrue();
        }
    }
}
