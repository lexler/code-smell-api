package com.codesmell.api.analysis;

public class TooManyAnalyses extends RuntimeException {

    public TooManyAnalyses(String message) {
        super(message);
    }
}
