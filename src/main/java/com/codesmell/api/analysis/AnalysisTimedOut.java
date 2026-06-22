package com.codesmell.api.analysis;

public class AnalysisTimedOut extends RuntimeException {

    public AnalysisTimedOut(String message) {
        super(message);
    }
}
