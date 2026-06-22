package com.codesmell.api.analysis.error;

public class AnalysisTimedOut extends RuntimeException {

    public AnalysisTimedOut(String message) {
        super(message);
    }
}
