package com.codesmell.api.analysis.error;

public class AnalysisRequestTooLarge extends RuntimeException {

    public AnalysisRequestTooLarge(String message) {
        super(message);
    }
}
