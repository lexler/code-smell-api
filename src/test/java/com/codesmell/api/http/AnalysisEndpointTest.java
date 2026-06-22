package com.codesmell.api.http;

import com.codesmell.api.CodeSmellApiApplication;
import com.codesmell.api.analysis.result.Violation;
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

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisEndpointTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void returnsViolationsForSubmittedCode() throws Exception {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.SERVLET)
            .properties("server.port=0")
            .run()) {
            var response = post(portOf(context), fixture("DeadCode.java"));

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(violationsFrom(response.body()))
                .extracting(Violation::rule)
                .contains("UnusedLocalVariable");
        }
    }

    @Test
    void returnsEmptyViolationsForCleanCode() throws Exception {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.SERVLET)
            .properties("server.port=0")
            .run()) {
            var response = post(portOf(context), fixture("CleanCode.java"));

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(violationsFrom(response.body())).isEmpty();
        }
    }

    @Test
    void rejectsEmptyBody() throws Exception {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.SERVLET)
            .properties("server.port=0")
            .run()) {
            var response = post(portOf(context), "");

            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).isEqualTo("{\"error\":\"code is required\"}");
        }
    }

    @Test
    void rejectsUnparseableCode() throws Exception {
        try (var context = new SpringApplicationBuilder(CodeSmellApiApplication.class)
            .web(WebApplicationType.SERVLET)
            .properties("server.port=0")
            .run()) {
            var response = post(portOf(context), "this is not Java");

            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).isEqualTo("{\"error\":\"code could not be parsed\"}");
        }
    }

    private int portOf(org.springframework.context.ConfigurableApplicationContext context) {
        return ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private HttpResponse<String> post(int port, String body) throws Exception {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/analyze"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    private List<Violation> violationsFrom(String body) throws IOException {
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
