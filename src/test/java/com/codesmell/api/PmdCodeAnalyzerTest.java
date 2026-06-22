package com.codesmell.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PmdCodeAnalyzerTest {

    private final PmdCodeAnalyzer analyzer = new PmdCodeAnalyzer();

    @Test
    void findsNoSmellsInCleanCode() {
        assertThat(analyzer.findSmells(fixture("CleanCode.java"))).isEmpty();
    }

    @Test
    void findsExpectedRulesInSmellyCode() {
        var expectedRules = Map.of(
            "DeadCode.java", "UnusedLocalVariable",
            "GlobalMutableState.java", "MutableStaticState",
            "MagicNumber.java", "AvoidLiteralsInIfCondition",
            "NestedIfCanyon.java", "AvoidDeeplyNestedIfStmts",
            "PrimitiveObsession.java", "UseObjectForClearerAPI",
            "SwallowedException.java", "EmptyCatchBlock"
        );

        expectedRules.forEach((fixture, rule) ->
            assertThat(analyzer.findSmells(fixture(fixture)))
                .extracting(SmellFinding::rule)
                .contains(rule)
        );
    }

    @Test
    void exposesLineMessageAndSeverity() {
        var findings = analyzer.findSmells(fixture("DeadCode.java"));

        assertThat(findings).first()
            .satisfies(finding -> {
                assertThat(finding.line()).isPositive();
                assertThat(finding.message()).isNotBlank();
                assertThat(finding.severity()).isBetween(1, 5);
            });
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
