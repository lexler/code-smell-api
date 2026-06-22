package com.codesmell.api.analysis.error;

public class TooManyAnalyses extends RuntimeException {

    public TooManyAnalyses(String message) {
        super(message);
    }
}
