package com.codesmell.api;

public class AnalysisRequestTooLarge extends RuntimeException {

    public AnalysisRequestTooLarge(String message) {
        super(message);
    }
}
