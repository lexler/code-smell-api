package com.codesmell.api.analysis;

public class AnalysisFailed extends RuntimeException {

    public AnalysisFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
