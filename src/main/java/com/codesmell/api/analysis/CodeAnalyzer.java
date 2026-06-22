package com.codesmell.api.analysis;

import java.util.List;

public interface CodeAnalyzer {

    List<SmellFinding> findSmells(String code);
}
