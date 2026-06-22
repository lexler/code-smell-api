# Code Smell API

HTTP API for finding Java code smells in submitted source.

```text
POST /analyze { "code": "..." }
  -> [{ "rule": "...", "message": "...", "line": 1, "severity": 3 }]
```

## Run

```bash
mise install
mvn test
mvn spring-boot:run
```

PMD is embedded as a library and pinned in `pom.xml`.

## Try It

```bash
jq -Rs '{code:.}' src/test/resources/smells/DeadCode.java \
  | curl -s -X POST http://localhost:8080/analyze \
      -H 'Content-Type: application/json' \
      --data-binary @-
```

Expected rule: `UnusedLocalVariable`.

## Configuration

- `code-smell.max-code-chars`, default `100000`
- `code-smell.analysis-timeout-ms`, default `3000`
- `code-smell.max-concurrent-analyses`, default `2`
- `code-smell.api-key`, default empty; when set, requests must send `X-API-Key`
