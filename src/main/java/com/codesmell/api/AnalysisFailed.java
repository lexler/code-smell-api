package com.codesmell.api;

public class AnalysisFailed extends RuntimeException {

    public AnalysisFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
