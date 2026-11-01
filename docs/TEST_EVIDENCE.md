# Test Evidence

Real results from running this project, not a plan.

## 1. Build and automated tests

```bash
mvn clean test
```

| Environment | Result |
|---|---|
| JDK 22 (Temurin) + Maven 3.9.16 | BUILD SUCCESS — Tests run: 5, Failures: 0, Errors: 0 |
| JDK 27 + Maven 3.9.16 | BUILD SUCCESS — Tests run: 5, Failures: 0, Errors: 0 |

> **IDE portability check (2026-11-01):** the sources no longer use Lombok, so they compile
> with both `javac` (Maven) and the Eclipse compiler used by Spring Tools 5.4.0 (ECJ 3.47).
> A direct ECJ build of all 44 sources finished with **0 errors** (3 `serialVersionUID`
> warnings), so the project compiles in Eclipse/IntelliJ/VS Code without any plugin or
> annotation-processing setup.

* `CoursehubApplicationTests.contextLoads` — starts the whole Spring context on H2, so it also
  validates every entity mapping and every JPQL query.
* `CategoryApiTests` — 4 MockMvc tests: 201, 400 with `fieldErrors`, 409 duplicate, 404 unknown id.

## 2. End-to-end replay of the Postman collection

The application was started against an in-memory H2 database (`demo` profile) and the **76
scenarios of `postman/CourseHub.postman_collection.json`** were replayed over real HTTP with
`scripts/validate-api.ps1`.

| | |
|---|---|
| Date | 2026-11-01 |
| Runtime | Java 22, Spring Boot 3.5.0, H2 2.3.232 (in-memory) |
| Port | 8081 (port 8080 was already used by an Oracle XE listener on the test machine) |
| Result | **76 / 76 passed** |

Coverage of the run: 9 create, 14 read, 6 update, 9 custom-query checks, 25 error cases
(400/404/409/422) and 13 delete/cleanup checks.

## 3. Full result log

| Check | Expected | Actual | Result |
|---|---|---|---|
| POST /api/categories -> 201 | 201 | 201 | PASS |
| POST /api/instructors -> 201 | 201 | 201 | PASS |
| POST /api/tags (java) -> 201 | 201 | 201 | PASS |
| POST /api/tags (spring) -> 201 | 201 | 201 | PASS |
| POST /api/courses -> 201, relations resolved | 201 | 201 | PASS |
| POST /api/students -> 201 | 201 | 201 | PASS |
| POST /api/enrollments -> 201 | 201 | 201 | PASS |
| PUT /api/enrollments/{id} progress 50 -> 200 | 200 | 200 | PASS |
| POST /api/reviews -> 201 | 201 | 201 | PASS |
| GET /api/categories -> 200 non-empty | 200 | 200 | PASS |
| GET /api/instructors -> 200 non-empty | 200 | 200 | PASS |
| GET /api/tags -> 200 non-empty | 200 | 200 | PASS |
| GET /api/students -> 200 non-empty | 200 | 200 | PASS |
| GET /api/courses -> 200 non-empty | 200 | 200 | PASS |
| GET /api/enrollments -> 200 non-empty | 200 | 200 | PASS |
| GET /api/reviews -> 200 non-empty | 200 | 200 | PASS |
| GET /api/categories/{id} -> 200 | 200 | 200 | PASS |
| GET /api/instructors/{id} -> 200 | 200 | 200 | PASS |
| GET /api/tags/{id} -> 200 | 200 | 200 | PASS |
| GET /api/students/{id} -> 200 | 200 | 200 | PASS |
| GET /api/courses/{id} -> 200 | 200 | 200 | PASS |
| GET /api/enrollments/{id} -> 200 | 200 | 200 | PASS |
| GET /api/reviews/{id} -> 200 | 200 | 200 | PASS |
| PUT /api/categories/{id} -> 200 | 200 | 200 | PASS |
| PUT /api/instructors/{id} -> 200 | 200 | 200 | PASS |
| PUT /api/tags/{id} -> 200 | 200 | 200 | PASS |
| PUT /api/students/{id} -> 200 | 200 | 200 | PASS |
| PUT /api/courses/{id} -> 200 (new price) | 200 | 200 | PASS |
| PUT /api/reviews/{id} -> 200 (rating 4) | 200 | 200 | PASS |
| Q1 search keyword+price -> 200 contains course | 200 | 200 | PASS |
| Q1b search budget too low -> 200 empty | 200 | 200 | PASS |
| Q2 class roster -> 200 contains student | 200 | 200 | PASS |
| Q3 popular courses -> 200 sorted desc | 200 | 200 | PASS |
| Q4 instructor revenue -> 39.99 | 200 | 200 | PASS |
| Q5 course average rating -> 4.0 | 200 | 200 | PASS |
| Q6 courses of a category -> 200 contains course | 200 | 200 | PASS |
| Q7 enrollments of a student -> 200 contains enrollment | 200 | 200 | PASS |
| Q8 reviews of a course -> 200 contains review | 200 | 200 | PASS |
| E01 404 unknown category | 404 | 404 | PASS |
| E02 404 course with unknown category | 404 | 404 | PASS |
| E03 404 unknown URL | 404 | 404 | PASS |
| E04 400 blank category name + fieldErrors | 400 | 400 | PASS |
| E05 400 invalid course (3 field errors) | 400 | 400 | PASS |
| E06 400 invalid enum level=EXPERT | 400 | 400 | PASS |
| E07 400 malformed JSON | 400 | 400 | PASS |
| E08 400 invalid email | 400 | 400 | PASS |
| E09 400 id is not a number | 400 | 400 | PASS |
| E10 400 review rating out of range | 400 | 400 | PASS |
| E11 409 duplicate category name | 409 | 409 | PASS |
| E12 409 duplicate student email | 409 | 409 | PASS |
| E13 409 student already enrolled | 409 | 409 | PASS |
| E14 409 student already reviewed this course | 409 | 409 | PASS |
| E15 409 cannot delete category that has courses | 409 | 409 | PASS |
| E16 409 cannot delete instructor that has courses | 409 | 409 | PASS |
| E17 409 cannot delete course that has enrollments | 409 | 409 | PASS |
| E18 409 cannot delete student that has enrollments | 409 | 409 | PASS |
| E19 (setup) create second student -> 201 | 201 | 201 | PASS |
| E20 422 review by a non-enrolled student | 422 | 422 | PASS |
| E21 (setup) enroll second student -> 201 | 201 | 201 | PASS |
| E22 (setup) cancel enrollment -> 200 | 200 | 200 | PASS |
| E23 422 cancelled enrollment cannot be modified | 422 | 422 | PASS |
| E24 cancelled student is not in the roster | 200 | 200 | PASS |
| E25 100% progress auto-completes enrollment | 200 | 200 | PASS |
| DELETE /api/tags/{id} (detached first) -> 204 | 204 | 204 | PASS |
| Course now has only 1 tag | 200 | 200 | PASS |
| DELETE /api/reviews/{id} -> 204 | 204 | 204 | PASS |
| DELETE enrollment B -> 204 | 204 | 204 | PASS |
| DELETE enrollment A -> 204 | 204 | 204 | PASS |
| DELETE student B -> 204 | 204 | 204 | PASS |
| DELETE student A -> 204 | 204 | 204 | PASS |
| DELETE course -> 204 | 204 | 204 | PASS |
| DELETE tag 1 -> 204 | 204 | 204 | PASS |
| DELETE instructor -> 204 | 204 | 204 | PASS |
| DELETE category -> 204 | 204 | 204 | PASS |
| Course is really gone -> 404 | 404 | 404 | PASS |
| Deleting again -> 404 | 404 | 404 | PASS |

## 4. Reproduce

```bash
# 1. build and unit tests (H2, no MySQL needed)
mvn clean test

# 2. start the API on H2 (test classpath, test profile)
mvn clean spring-boot:test-run -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments=--server.port=8081
# or, equivalently:  pwsh -File scripts/run-h2.ps1 -Port 8081

# 3. in another terminal, replay the 76 scenarios
pwsh -File scripts/validate-api.ps1
```

Or run the collection in Postman: import `postman/CourseHub.postman_collection.json`, check the
`baseUrl` variable, then **Run collection**.

## 5. What this evidence does not cover

* **MySQL** was not exercised in this run; the replay used H2. The MySQL settings in
  `application.properties` are standard Spring Boot and `createDatabaseIfNotExist=true` creates
  the schema, but a run against a live MySQL server is still to be done.
* **Postman Runner screenshots** must come from your own Postman run; this file is terminal
  evidence, not a substitute for the screenshots DS1 asks for.
