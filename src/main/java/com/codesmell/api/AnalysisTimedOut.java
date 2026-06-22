package com.codesmell.api;

public class AnalysisTimedOut extends RuntimeException {

    public AnalysisTimedOut(String message) {
        super(message);
    }
}
