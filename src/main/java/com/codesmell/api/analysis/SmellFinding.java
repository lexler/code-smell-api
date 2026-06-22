package com.codesmell.api.analysis;

public record SmellFinding(
    String rule,
    String message,
    int line,
    int severity
) {}
