package com.codesmell.api.analysis.error;

public class InvalidAnalysisRequest extends RuntimeException {

    public InvalidAnalysisRequest(String message) {
        super(message);
    }
}
