package com.codesmell.api.analysis;

import com.codesmell.api.analysis.error.AnalysisFailed;
import com.codesmell.api.analysis.error.AnalysisRequestTooLarge;
import com.codesmell.api.analysis.error.AnalysisTimedOut;
import com.codesmell.api.analysis.error.InvalidAnalysisRequest;
import com.codesmell.api.analysis.error.TooManyAnalyses;
import com.codesmell.api.analysis.result.Violation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class AnalysisService {

    private final CodeAnalyzer analyzer;
    private final int maxCodeChars;
    private final Duration timeout;
    private final ExecutorService executor;

    @Autowired
    public AnalysisService(
        CodeAnalyzer analyzer,
        @Value("${code-smell.max-code-chars:100000}") int maxCodeChars,
        @Value("${code-smell.analysis-timeout-ms:3000}") long timeoutMillis,
        @Value("${code-smell.max-concurrent-analyses:2}") int maxConcurrentAnalyses
    ) {
        this(analyzer, maxCodeChars, Duration.ofMillis(timeoutMillis), maxConcurrentAnalyses);
    }

    AnalysisService(CodeAnalyzer analyzer, int maxCodeChars, Duration timeout, int maxConcurrentAnalyses) {
        this.analyzer = analyzer;
        this.maxCodeChars = maxCodeChars;
        this.timeout = timeout;
        this.executor = new ThreadPoolExecutor(
            maxConcurrentAnalyses,
            maxConcurrentAnalyses,
            0L,
            TimeUnit.MILLISECONDS,
            new SynchronousQueue<>()
        );
    }

    public List<Violation> analyze(String code) {
        rejectMissing(code);
        rejectOversized(code);
        return analyzeCode(code);
    }

    private List<Violation> analyzeCode(String code) {
        var analysis = submitAnalysis(code);
        try {
            return analysis.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException error) {
            analysis.cancel(true);
            throw new AnalysisTimedOut("analysis timed out");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AnalysisTimedOut("analysis was interrupted");
        } catch (java.util.concurrent.ExecutionException error) {
            throw new AnalysisFailed("analysis failed", error.getCause());
        }
    }

    private Future<List<Violation>> submitAnalysis(String code) {
        try {
            return executor.submit(() -> analyzer.analyze(code));
        } catch (RejectedExecutionException error) {
            throw new TooManyAnalyses("too many analyses are already running");
        }
    }

    private void rejectMissing(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidAnalysisRequest("code is required");
        }
    }

    private void rejectOversized(String code) {
        if (code.length() > maxCodeChars) {
            throw new AnalysisRequestTooLarge("code is too large");
        }
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
    }
}
