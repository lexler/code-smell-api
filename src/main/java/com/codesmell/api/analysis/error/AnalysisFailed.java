package com.codesmell.api.analysis.error;

public class AnalysisFailed extends RuntimeException {

    public AnalysisFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
