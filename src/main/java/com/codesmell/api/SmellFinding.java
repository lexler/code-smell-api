package com.codesmell.api;

public record SmellFinding(
    String rule,
    String message,
    int line,
    int severity
) {}
