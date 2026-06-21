Generate API documentation as a standalone HTML file from the running Spring Boot app's OpenAPI spec.

## Steps

1. **Check if the app is running** on port 8080:
   ```bash
   curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/smart-seaman-swagger
   ```
   - If not running (connection refused or non-200), start it in the background:
     ```bash
     ./mvnw spring-boot:run > /tmp/smart-seaman-app.log 2>&1 &
     ```
   - Wait up to 60 seconds for the app to become ready by polling `http://localhost:8080/smart-seaman-swagger` every 5 seconds.

2. **Fetch the OpenAPI JSON spec**:
   ```bash
   curl -s http://localhost:8080/smart-seaman-swagger -o /tmp/smart-seaman-openapi.json
   ```

3. **Generate a standalone HTML file** using one of these methods (try in order):

   **Method A — redoc-cli (npx, no install needed)**:
   ```bash
   npx --yes redoc-cli bundle /tmp/smart-seaman-openapi.json \
     --title "Smart Seaman Mobile API" \
     --output documents/smart-seaman-api-spec.html
   ```

   **Method B — openapi-generator-cli (npx)**:
   ```bash
   npx --yes @openapitools/openapi-generator-cli generate \
     -i /tmp/smart-seaman-openapi.json \
     -g html2 \
     -o /tmp/smart-seaman-html-docs && \
   cp /tmp/smart-seaman-html-docs/index.html documents/smart-seaman-api-spec.html
   ```

   **Method C — Python fallback** (always available, builds a Swagger UI HTML):
   ```bash
   python3 - <<'PYEOF'
   import json, html, sys

   with open('/tmp/smart-seaman-openapi.json') as f:
       spec = json.load(f)

   spec_json = json.dumps(spec)

   page = f"""<!DOCTYPE html>
   <html lang="en">
   <head>
     <meta charset="UTF-8">
     <meta name="viewport" content="width=device-width, initial-scale=1">
     <title>Smart Seaman Mobile API</title>
     <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
   </head>
   <body>
     <div id="swagger-ui"></div>
     <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
     <script>
       SwaggerUIBundle({{
         spec: {spec_json},
         dom_id: '#swagger-ui',
         presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset],
         layout: 'BaseLayout',
         deepLinking: true,
         showExtensions: true,
         showCommonExtensions: true
       }});
     </script>
   </body>
   </html>"""

   with open('documents/smart-seaman-api-spec.html', 'w') as f:
       f.write(page)

   print("HTML written to documents/smart-seaman-api-spec.html")
   PYEOF
   ```

4. **Report the result**: Tell the user the output file path (`documents/smart-seaman-api-spec.html`) and its file size. If the app was started by this skill, mention that it is still running in the background.

## Notes
- If the app was already running before this skill was invoked, do NOT stop it after generating the docs.
- If the app was started by this skill solely to generate docs, tell the user it's still running and they can stop it with `kill $(lsof -ti:8080)` if needed.
- Always prefer Method A (redoc-cli) for the best-looking output; fall back to Method C if npx is unavailable.
- The output file `documents/smart-seaman-api-spec.html` is gitignored via the project's existing pattern — remind the user to commit it manually if they want it tracked.
