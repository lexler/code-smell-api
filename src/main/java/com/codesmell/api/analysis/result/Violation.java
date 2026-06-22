package com.codesmell.api.analysis.result;

public record Violation(
    String rule,
    String message,
    int line,
    int severity
) {}
