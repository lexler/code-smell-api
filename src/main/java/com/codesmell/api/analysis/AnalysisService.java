package com.codesmell.api.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class AnalysisService {

    private final CodeAnalyzer analyzer;
    private final int maxCodeChars;
    private final Duration timeout;
    private final Semaphore analysisSlots;
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
        this.analysisSlots = new Semaphore(maxConcurrentAnalyses);
        this.executor = Executors.newFixedThreadPool(maxConcurrentAnalyses);
    }

    public List<SmellFinding> analyze(String code) {
        rejectMissing(code);
        rejectOversized(code);
        acquireAnalysisSlot();
        try {
            return findSmells(code);
        } finally {
            analysisSlots.release();
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

    private void acquireAnalysisSlot() {
        if (!analysisSlots.tryAcquire()) {
            throw new TooManyAnalyses("too many analyses are already running");
        }
    }

    private List<SmellFinding> findSmells(String code) {
        var analysis = executor.submit(() -> analyzer.findSmells(code));
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

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
    }
}
