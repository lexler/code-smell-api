package com.codesmell.api.analysis;

import com.codesmell.api.analysis.result.Violation;

import java.util.List;

public interface CodeAnalyzer {

    List<Violation> analyze(String code);
}
