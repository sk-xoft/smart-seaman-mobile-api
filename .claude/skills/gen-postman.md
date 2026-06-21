# gen-postman

Regenerate the Postman collection and HTML API spec from the current controller source code.

## Steps

1. Scan all files under `src/main/java/com/seaman/controller/` to find every `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@PutMapping` annotation and extract:
   - HTTP method and path
   - Request body DTO class (look for `@RequestBody`)
   - Query/path parameters (look for `@RequestParam`, `@PathVariable`)
   - Whether the endpoint is public (check `SecurityConfiguration.java` permitAll list)

2. Scan `src/main/java/com/seaman/model/` for all DTO fields.

3. Overwrite `documents/smart-seaman-api.postman_collection.json` with an updated Postman Collection v2.1 JSON reflecting the current endpoints.

4. Overwrite `documents/smart-seaman-api-spec.html` with an updated self-contained HTML API reference.

5. Report a summary: how many endpoints were found per controller, and which files were updated.
