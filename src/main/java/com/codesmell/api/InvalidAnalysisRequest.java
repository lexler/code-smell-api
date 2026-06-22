package com.codesmell.api;

public class InvalidAnalysisRequest extends RuntimeException {

    public InvalidAnalysisRequest(String message) {
        super(message);
    }
}
