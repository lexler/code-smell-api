package com.codesmell.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AnalysisController {

    private final AnalysisService analyzer;

    public AnalysisController(AnalysisService analyzer) {
        this.analyzer = analyzer;
    }

    @PostMapping("/analyze")
    public List<SmellFinding> analyze(@RequestBody AnalysisRequest request) {
        return analyzer.analyze(request);
    }

    @ExceptionHandler(InvalidAnalysisRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidAnalysisRequest(InvalidAnalysisRequest error) {
        return Map.of("error", error.getMessage());
    }

    @ExceptionHandler(AnalysisRequestTooLarge.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, String> analysisRequestTooLarge(AnalysisRequestTooLarge error) {
        return Map.of("error", error.getMessage());
    }

    @ExceptionHandler(TooManyAnalyses.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> tooManyAnalyses(TooManyAnalyses error) {
        return Map.of("error", error.getMessage());
    }

    @ExceptionHandler(AnalysisTimedOut.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> analysisTimedOut(AnalysisTimedOut error) {
        return Map.of("error", error.getMessage());
    }
}
