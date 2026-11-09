# Postman screenshots

DS1 asks for **exported screenshots of successful Postman tests**. Run the collection in
Postman, then save the captures in this folder with these names:

| File | What to capture |
|---|---|
| `01-runner-summary.png` | Collection Runner result: **76 requests, 0 failures** |
| `02-create-201.png` | `POST /api/courses` → **201**, body showing `category`, `instructor` and `tags` |
| `03-validation-400.png` | `POST /api/courses` with an invalid body → **400** and `fieldErrors` |
| `04-not-found-404.png` | `GET /api/categories/999999` → **404** |
| `05-conflict-409.png` | Duplicate category name → **409** |
| `06-business-422.png` | Modifying a cancelled enrollment → **422** |
| `07-custom-query.png` | `GET /api/courses/popular?limit=3` or the revenue endpoint |

How to reproduce:

1. Start the API: `mvn spring-boot:run` (the MySQL database `coursehub` is created automatically),
   or `pwsh -File scripts/run-h2.ps1` / the `demo` profile if MySQL is not available.
2. In Postman, import `postman/CourseHub.postman_collection.json`.
3. Check the collection variable `baseUrl` (default `http://localhost:8081`, matching the `demo` run command).
4. Select the collection → **Run** → keep the order → **Run CourseHub API - DS1**.
5. Screenshot the summary, then open the individual requests above and screenshot each response.

The collection is re-runnable: its first request sets a timestamp `suffix` so names and emails
stay unique between runs.
