package com.codesmell.api;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
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

    @Override
    public List<SmellFinding> findSmells(String code) {
        var configuration = new PMDConfiguration();
        configuration.setThreads(1);
        configuration.setIgnoreIncrementalAnalysis(true);

        try (var analysis = PmdAnalysis.create(configuration)) {
            analysis.addRuleSet(analysis.newRuleSetLoader().loadFromString(RULESET_RESOURCE, rulesetXml()));
            analysis.files().addFile(sourceFile(code));
            return analysis.performAnalysisAndCollectReport().getViolations().stream()
                .sorted(Comparator.comparingInt(RuleViolation::getBeginLine).thenComparing(violation -> violation.getRule().getName()))
                .map(this::findingFrom)
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

    private SmellFinding findingFrom(RuleViolation violation) {
        return new SmellFinding(
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
