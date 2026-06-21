# docker-build

Build and tag a Docker image for the Smart Seaman Mobile API.

## Steps

1. Read the current version from `pom.xml` (`<version>` tag) to use as the image tag.
2. Package the JAR first (skip tests for speed):
   ```bash
   ./mvnw clean package -DskipTests
   ```
3. Build the Docker image:
   ```bash
   docker build -t xoftspace/smart-seaman-mobile-api:<version> .
   ```
   Replace `<version>` with the value from step 1.
4. Also tag as `latest`:
   ```bash
   docker tag xoftspace/smart-seaman-mobile-api:<version> xoftspace/smart-seaman-mobile-api:latest
   ```
5. Report the image name, tag, and size (`docker images xoftspace/smart-seaman-mobile-api`).
6. Remind the user of the run command:
   ```bash
   docker run --name smart-seaman-mobile-api -d \
     -e COMPANY='smart-seaman' \
     -e ENV='dev' \
     -it -p 30000:8080/tcp \
     xoftspace/smart-seaman-mobile-api:<version>
   ```
