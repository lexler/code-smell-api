package com.codesmell.api.analysis;

public class AnalysisRequestTooLarge extends RuntimeException {

    public AnalysisRequestTooLarge(String message) {
        super(message);
    }
}
