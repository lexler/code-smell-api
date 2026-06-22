package com.codesmell.api.analysis;

import com.codesmell.api.analysis.error.AnalysisRequestTooLarge;
import com.codesmell.api.analysis.error.AnalysisTimedOut;
import com.codesmell.api.analysis.error.InvalidAnalysisRequest;
import com.codesmell.api.analysis.error.TooManyAnalyses;
import com.codesmell.api.analysis.result.Violation;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisServiceTest {

    @Test
    void returnsViolationsFromTheAnalyzer() {
        var violation = new Violation("UnusedLocalVariable", "Avoid unused local variables", 4, 3);
        var service = new AnalysisService(code -> List.of(violation), 100, Duration.ofSeconds(1), 1);

        assertThat(service.analyze("class Sample {}")).containsExactly(violation);
    }

    @Test
    void rejectsMissingCode() {
        var service = new AnalysisService(code -> List.of(), 100, Duration.ofSeconds(1), 1);

        assertThatThrownBy(() -> service.analyze(" "))
            .isInstanceOf(InvalidAnalysisRequest.class)
            .hasMessage("code is required");
    }

    @Test
    void rejectsOversizedCode() {
        var service = new AnalysisService(code -> List.of(), 5, Duration.ofSeconds(1), 1);

        assertThatThrownBy(() -> service.analyze("class Sample {}"))
            .isInstanceOf(AnalysisRequestTooLarge.class)
            .hasMessage("code is too large");
    }

    @Test
    void rejectsWhenAllAnalysisSlotsAreBusy() throws InterruptedException {
        var analyzerStarted = new CountDownLatch(1);
        var releaseAnalyzer = new CountDownLatch(1);
        var service = new AnalysisService(code -> {
            analyzerStarted.countDown();
            await(releaseAnalyzer);
            return List.of();
        }, 100, Duration.ofSeconds(2), 1);

        var firstAnalysis = new Thread(() -> service.analyze("class First {}"));
        firstAnalysis.start();
        analyzerStarted.await();

        assertThatThrownBy(() -> service.analyze("class Second {}"))
            .isInstanceOf(TooManyAnalyses.class)
            .hasMessage("too many analyses are already running");

        releaseAnalyzer.countDown();
        firstAnalysis.join();
    }

    @Test
    void timesOutSlowAnalysis() {
        var service = new AnalysisService(code -> {
            sleep(Duration.ofSeconds(1));
            return List.of();
        }, 100, Duration.ofMillis(10), 1);

        assertThatThrownBy(() -> service.analyze("class Sample {}"))
            .isInstanceOf(AnalysisTimedOut.class)
            .hasMessage("analysis timed out");
    }

    @Test
    void keepsSlotBusyUntilTimedOutAnalysisActuallyStops() throws InterruptedException {
        var analyzerStarted = new CountDownLatch(1);
        var releaseAnalyzer = new CountDownLatch(1);
        var service = new AnalysisService(code -> {
            analyzerStarted.countDown();
            awaitIgnoringInterrupt(releaseAnalyzer);
            return List.of();
        }, 100, Duration.ofMillis(10), 1);

        assertThatThrownBy(() -> service.analyze("class First {}"))
            .isInstanceOf(AnalysisTimedOut.class)
            .hasMessage("analysis timed out");

        analyzerStarted.await();

        assertThatThrownBy(() -> service.analyze("class Second {}"))
            .isInstanceOf(TooManyAnalyses.class)
            .hasMessage("too many analyses are already running");

        releaseAnalyzer.countDown();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private static void awaitIgnoringInterrupt(CountDownLatch latch) {
        boolean interrupted = false;
        while (true) {
            try {
                latch.await();
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                return;
            } catch (InterruptedException error) {
                interrupted = true;
            }
        }
    }
}
