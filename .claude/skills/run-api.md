# run-api

Start the Smart Seaman Mobile API Spring Boot development server.

## Steps

1. Check that the working directory is the project root (contains `mvnw`).
2. Run the application with:
   ```bash
   ./mvnw spring-boot:run
   ```
   Run this in the background so the server stays up.
3. Wait for the log line `Started ... in ... seconds` to confirm startup.
4. Report the base URL: `http://localhost:8080`
5. Remind the user they can call `/health` to verify: `curl http://localhost:8080/health`
