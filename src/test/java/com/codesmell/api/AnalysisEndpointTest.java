package com.codesmell.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisEndpointTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void returnsFindingsForSubmittedCode() throws Exception {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.SERVLET)
            .properties("server.port=0")
            .run()) {
            var response = post(portOf(context), Map.of("code", fixture("DeadCode.java")));

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(findingsFrom(response.body()))
                .extracting(SmellFinding::rule)
                .contains("UnusedLocalVariable");
        }
    }

    @Test
    void returnsEmptyFindingsForCleanCode() throws Exception {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.SERVLET)
            .properties("server.port=0")
            .run()) {
            var response = post(portOf(context), Map.of("code", fixture("CleanCode.java")));

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(findingsFrom(response.body())).isEmpty();
        }
    }

    private int portOf(org.springframework.context.ConfigurableApplicationContext context) {
        return ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private HttpResponse<String> post(int port, Map<String, String> body) throws Exception {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/analyze"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    private List<SmellFinding> findingsFrom(String body) throws IOException {
        return json.readValue(body, new TypeReference<>() {});
    }

    private String fixture(String name) {
        var resource = "smells/" + name;
        try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("missing fixture: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }
}
