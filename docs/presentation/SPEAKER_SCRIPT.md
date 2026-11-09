# CourseHub — 5-minute presentation script

Deck: [`CourseHub_DS1_Presentation.pptx`](CourseHub_DS1_Presentation.pptx) · 10 slides · 16:9
The same text is in the **speaker notes** of every slide.

| # | Slide | Time | Length |
|---|---|---|---|
| 1 | Title | 0:00 – 0:15 | 15 s |
| 2 | Overview | 0:15 – 0:45 | 30 s |
| 3 | Architecture | 0:45 – 1:25 | 40 s |
| 4 | Data model | 1:25 – 2:05 | 40 s |
| 5 | JPA mapping | 2:05 – 2:40 | 35 s |
| 6 | Business rules | 2:40 – 3:20 | 40 s |
| 7 | Custom queries | 3:20 – 4:00 | 40 s |
| 8 | Validation & errors | 4:00 – 4:30 | 30 s |
| 9 | Testing | 4:30 – 4:50 | 20 s |
| 10 | Wrap-up | 4:50 – 5:00 | 10 s |

---

## 1 — Title (0:00 – 0:15)

> Good morning. I'm Mahmoud, and this is **CourseHub** — the backend of an online course
> platform, built for DS1 with Spring Boot, Spring Data JPA and MySQL. In five minutes I'll
> show the data model, the layered design, the business rules, the custom queries, and the tests.

## 2 — Overview (0:15 – 0:45)

> CourseHub lets instructors publish courses, and students enroll, track progress and review
> them. There are **seven linked entities**, more than **forty REST endpoints** with full CRUD,
> and a Postman collection of **seventy-six requests**. There is no frontend — Postman is the
> client.

## 3 — Architecture (0:45 – 1:25)

> Every request crosses four layers. The **controller** only handles HTTP: routing, status
> codes and validation. The **service** owns the rules and the transaction. The **repository**
> is a Spring Data interface — I write no SQL for CRUD. The **model** maps seven entities to
> tables. Keeping these separate is what makes the project easy to test.

## 4 — Data model (1:25 – 2:05)

> The data model uses all three relationship styles. Category and Instructor are
> **one-to-many** to Course. Course and Tag are **many-to-many** through `course_tags`.
> Enrollment and Review are **association entities**: they carry their own columns — progress
> and status, or rating and comment — plus a unique constraint on student and course, which is
> what blocks double enrollment and double review.

## 5 — JPA mapping (2:05 – 2:40)

> In JPA the relationships are declared once, in the entity. `@ManyToOne` with `@JoinColumn`
> creates the foreign key. `@ManyToMany` with `@JoinTable` creates the join table. `mappedBy`
> marks the inverse side, and `@JsonIgnore` stops infinite recursion when the entity is
> serialized. After that, Hibernate handles the joins.

## 6 — Business rules (2:40 – 3:20)

> Business rules stay in the service. A student cannot enroll twice — **409**. A cancelled
> enrollment is frozen — **422**. Reaching one hundred percent progress automatically completes
> the enrollment, and a completed enrollment is forced to one hundred percent. The controller
> never decides any of this.

## 7 — Custom queries (3:20 – 4:00)

> DS1 asks for three custom queries; I wrote **five**. Search filters by title and budget. The
> roster joins enrollments and excludes cancelled ones. Popularity groups by course, counts
> enrollments and limits the result with `Pageable`. Revenue sums course prices, and rating
> averages the reviews. All of them are **JPQL**, so they use entity names and fields, not
> table names.

## 8 — Validation and errors (4:00 – 4:30)

> Validation is declared on the entities — `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`,
> `@Past`, `@NotNull` — and triggered by `@Valid` on the controller. When something fails, one
> `RestControllerAdvice` converts it into the same JSON shape with the right status: **400** for
> validation or broken JSON, **404** for an unknown id, **409** for a duplicate, and **422** for
> a broken business rule.

## 9 — Testing (4:30 – 4:50)

> Finally, testing. The Postman collection has **seventy-six requests**: full CRUD, the five
> custom queries, and twenty-five error cases, each asserting the status and the body. **All
> seventy-six pass.** There are also five JUnit tests on H2. The same API runs on MySQL or H2
> by changing the profile only.

## 10 — Wrap-up (4:50 – 5:00)

> In the demo I create a category, an instructor, two tags and a course, then a student, enroll
> them, review the course and show the three statistics endpoints. The project covers the whole
> DS1 checklist. Thank you — I'm happy to take questions.

---

## If you have extra time / are asked to demo

1. `POST /api/categories` and `POST /api/instructors`
2. `POST /api/tags` twice, then `POST /api/courses` with `categoryId`, `instructorId`, `tagIds`
3. `POST /api/students`, then `POST /api/enrollments`
4. `PUT /api/enrollments/{id}` with `{"progress": 100}` → status becomes COMPLETED
5. `POST /api/reviews`
6. `GET /api/courses/popular`, `GET /api/courses/{id}/rating`, `GET /api/instructors/{id}/revenue`

## Questions

The 16 most likely questions (and short answers) are in
[`../STUDY_GUIDE.md`](../STUDY_GUIDE.md), section 6.
