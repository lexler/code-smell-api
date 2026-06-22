package com.codesmell.api;

public class TooManyAnalyses extends RuntimeException {

    public TooManyAnalyses(String message) {
        super(message);
    }
}
