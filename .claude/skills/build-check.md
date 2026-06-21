# build-check

Build the project and report any compilation errors or test failures.

## Steps

1. Run a full clean build including tests:
   ```bash
   ./mvnw clean install
   ```
2. If the build succeeds, report: "Build passed" and the JAR location under `target/`.
3. If the build fails, extract and summarize:
   - Compilation errors (file, line number, message)
   - Test failures (class name, test method, failure reason)
4. Suggest fixes for any errors found.
