# Code Smell API

Brand-neutral API for finding Java code smells.

```text
POST /sniff { "code": "..." }
  -> [{ "rule": "...", "message": "...", "line": 1, "severity": 3 }]
```

Caramelo-specific names, jokes, and diagnosis mapping belong in the consumer, not here.

## Run

```bash
mise install
mvn test
mvn spring-boot:run
```

PMD is embedded as a library and pinned in `pom.xml`.
