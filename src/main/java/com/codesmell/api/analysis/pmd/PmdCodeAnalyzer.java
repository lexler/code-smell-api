package com.codesmell.api.analysis.pmd;

import com.codesmell.api.analysis.CodeAnalyzer;
import com.codesmell.api.analysis.error.InvalidAnalysisRequest;
import com.codesmell.api.analysis.result.Violation;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Service
public class PmdCodeAnalyzer implements CodeAnalyzer {

    private static final String RULESET_RESOURCE = "pmd/ruleset.xml";
    private static final String ANALYZED_FILE_NAME = "SubmittedCode.java";

    private final RuleSet ruleset;

    public PmdCodeAnalyzer() {
        ruleset = loadRuleset();
    }

    @Override
    public List<Violation> analyze(String code) {
        var configuration = new PMDConfiguration();
        configuration.setThreads(1);
        configuration.setIgnoreIncrementalAnalysis(true);

        try (var analysis = PmdAnalysis.create(configuration)) {
            analysis.addRuleSet(RuleSet.copy(ruleset));
            analysis.files().addFile(sourceFile(code));
            var report = analysis.performAnalysisAndCollectReport();
            if (!report.getProcessingErrors().isEmpty()) {
                throw new InvalidAnalysisRequest("code could not be parsed");
            }
            return report.getViolations().stream()
                .sorted(Comparator.comparingInt(RuleViolation::getBeginLine).thenComparing(violation -> violation.getRule().getName()))
                .map(this::violationFrom)
                .toList();
        }
    }

    private TextFile sourceFile(String code) {
        return TextFile.forCharSeq(
            code,
            FileId.fromPathLikeString(ANALYZED_FILE_NAME),
            JavaLanguageModule.getInstance().getDefaultVersion()
        );
    }

    private RuleSet loadRuleset() {
        var configuration = new PMDConfiguration();
        try (var analysis = PmdAnalysis.create(configuration)) {
            return analysis.newRuleSetLoader().loadFromString(RULESET_RESOURCE, rulesetXml());
        }
    }

    private Violation violationFrom(RuleViolation violation) {
        return new Violation(
            violation.getRule().getName(),
            violation.getDescription(),
            violation.getBeginLine(),
            violation.getRule().getPriority().getPriority()
        );
    }

    private String rulesetXml() {
        try (var ruleset = getClass().getClassLoader().getResourceAsStream(RULESET_RESOURCE)) {
            if (ruleset == null) {
                throw new IllegalStateException("missing PMD ruleset");
            }
            return new String(ruleset.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }
}
