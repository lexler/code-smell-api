# Code Smell API

HTTP API for finding Java code smells in submitted source.

This is a learning/demo service. It is real and deployable, but it is not hardened for production or untrusted public traffic.
If you expose it, set an API key and cap `max-instances`.

```text
POST /analyze
Content-Type: text/plain

<Java source>
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
curl -X POST --data-binary @src/test/resources/smells/DeadCode.java \
  -H 'Content-Type: text/plain' \
  http://localhost:8080/analyze
```

Expected rule: `UnusedLocalVariable`.

## Configuration

- `code-smell.max-code-chars`, default `100000`
- `code-smell.analysis-timeout-ms`, default `3000`
- `code-smell.max-concurrent-analyses`, default `2`
- `code-smell.api-key`, default empty; when set, requests must send `X-API-Key`
- `deployed` profile requires `code-smell.api-key`
