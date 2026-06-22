package com.codesmell.api.analysis;

public class InvalidAnalysisRequest extends RuntimeException {

    public InvalidAnalysisRequest(String message) {
        super(message);
    }
}
