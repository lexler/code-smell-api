package com.codesmell.api;

import java.util.List;

public interface CodeAnalyzer {

    List<SmellFinding> findSmells(String code);
}
