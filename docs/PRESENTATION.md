# CourseHub – 5-minute presentation

Target: **5 minutes**, 8 slides. Each slide has the text to show and the words to say.
Every team member should be able to take any slide (DS1 grading criterion: *Presentation and
Understanding of the Project*).

---

## Slide 1 – Title (15 s)

**On screen**

> **CourseHub** — Online course platform API
> DS1 · Spring Boot · Spring Data JPA · MySQL · Postman
> *Your name(s)*

**Say:** "CourseHub is the backend of an online learning platform. Instructors publish
courses, students enroll and review them. The API is built with Spring Boot and Spring Data
JPA, stores data in MySQL, and is tested with Postman."

---

## Slide 2 – Context and data model (60 s)

**On screen** — the ER diagram from `README.md`:

```
CATEGORY 1───* COURSE *───1 INSTRUCTOR
                 *
                 │ many-to-many  (join table course_tags)
                 *
                TAG

STUDENT 1───* ENROLLMENT *───1 COURSE
STUDENT 1───* REVIEW     *───1 COURSE
```

**Say:** "Seven entities. The three relationship types required by the course are all here:
**one-to-many** between Category and Course, **many-to-many** between Course and Tag with a
join table `course_tags`, and **association entities** Enrollment and Review. Enrollment is an
entity and not a simple many-to-many because the link itself carries data: progress, status and
the enrollment date."

---

## Slide 3 – Architecture (45 s)

**On screen**

```
Postman → Controller → Service → Repository → MySQL
          HTTP/JSON    rules     Spring Data    tables
```

**Say:** "Four layers, as in Chapter 2. The controller only translates HTTP and JSON. The
service holds the business rules and the transactions. The repository is a Spring Data
interface — Spring writes the SQL. The model is mapped to MySQL tables by JPA annotations."

**If asked why a service layer:** "So the rules are testable and the controller stays thin;
the controller never touches a repository."

---

## Slide 4 – JPA modeling (60 s)

**On screen** — two short code extracts:

```java
// Course = owning side, holds the foreign key
@ManyToOne(fetch = FetchType.EAGER, optional = false)
@JoinColumn(name = "category_id", nullable = false)
private Category category;

// Category = inverse side
@OneToMany(mappedBy = "category")
@JsonIgnore
private List<Course> courses = new ArrayList<>();
```

```java
// Course = owning side of the join table
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "course_tags",
           joinColumns = @JoinColumn(name = "course_id"),
           inverseJoinColumns = @JoinColumn(name = "tag_id"))
private Set<Tag> tags = new HashSet<>();
```

**Say:** "The rule is: the `@ManyToOne` side owns the relationship and holds `@JoinColumn`;
`mappedBy` marks the inverse side. For many-to-many, a join table is created by `@JoinTable`.
`@JsonIgnore` on the inverse collections stops infinite JSON recursion. Validation is declared
with `@NotBlank`, `@Email`, `@Min`… and the database constraints with `@Column(nullable,
unique)`."

---

## Slide 5 – Repository layer and custom queries (60 s)

**On screen** — the 5 business queries:

| Business need | Endpoint | Technique |
|---|---|---|
| Catalogue search by title + budget | `GET /api/courses/search?keyword=&maxPrice=` | JPQL `LIKE` |
| Class roster (cancelled excluded) | `GET /api/courses/{id}/students` | JPQL `JOIN` |
| Popular courses ranking | `GET /api/courses/popular?limit=` | `GROUP BY` + aggregated columns |
| Instructor revenue | `GET /api/instructors/{id}/revenue` | `SUM` |
| Course average rating | `GET /api/courses/{id}/rating` | `AVG` |

**Say:** "DS1 asks for three custom queries; we wrote five, each answering a real business
question. They use JPQL, so they are written against entities and fields, not tables —
Hibernate translates them to SQL. The popularity query is the most interesting: it groups
enrollments by course, counts them, orders descending, and returns the aggregated columns
directly, limited by a `Pageable`."

---

## Slide 6 – Validation and error handling (60 s)

**On screen** — one 400 response:

```json
{
  "timestamp": "2026-11-01T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/courses",
  "fieldErrors": {
    "title": "Title must be between 3 and 150 characters",
    "price": "Price cannot be negative",
    "durationHours": "Duration must be at least 1 hour"
  }
}
```

**Say:** "`@Valid` on the request body triggers Bean Validation before the method runs. One
`@RestControllerAdvice` turns every exception into the same JSON shape. The status codes carry
the meaning: **400** malformed or invalid request, **404** unknown id, **409** duplicate or
still referenced, **422** the request is valid but a business rule refuses it — for example
modifying a cancelled enrollment."

---

## Slide 7 – Demo (45 s)

**On screen:** Postman Runner, all requests green.

1. Run the collection → show the summary (76 requests, 0 failures).
2. Open one **201**, one **400 with `fieldErrors`**, one **409**, one **422**.

**Say:** "The collection is ordered: it creates a full dataset, reads it, updates it, runs the
custom queries, checks 25 error cases, then deletes everything. It is re-runnable: the first
request sets a timestamp suffix so names stay unique."

---

## Slide 8 – Conclusion (15 s)

**On screen**

> 7 entities · 5 relationship mappings · 5 custom JPQL queries · 40+ endpoints
> 400 / 404 / 409 / 422 handled centrally · 76 Postman tests · JUnit + H2 tests

**Say:** "Everything DS1 asks for is covered and the API was verified end to end. Next steps
would be authentication, pagination, and a dedicated response model everywhere."

---

## Likely questions (short answers)

| Question | Answer |
|---|---|
| Why a service layer? | Controllers handle HTTP only; services own business rules and transactions. |
| Owning side vs `mappedBy`? | The owning side holds the foreign key / join table; `mappedBy` is the mirror. |
| Why is Enrollment an entity? | The relation carries its own data (progress, status, date). |
| Why `@JsonIgnore`? | To break infinite recursion between bidirectional relations. |
| EAGER vs LAZY? | Eager loads with the entity; lazy loads on first access and needs an open session. |
| `ddl-auto=update` vs `create`? | `update` adapts the schema and keeps data; `create` drops and recreates it. |
| JPQL vs SQL? | JPQL uses entities and fields; Hibernate generates the SQL dialect. |
| How is the ranking query built? | It selects `c.id`, `c.title` and `COUNT(e)`, groups by course, orders by the count and limits the rows with `Pageable`. |
| How are related rows sent? | The client sends ids (`categoryId`, `instructorId`, `tagIds`, `studentId`, `courseId`); a small link method on the entity turns each id into a reference, and the service loads the real row (404 if unknown). |
| Why env variables for the DB password? | Secrets must never be committed to Git. |
| `javax` vs `jakarta`? | Java EE became Jakarta EE; Spring Boot 3 uses `jakarta.*`. |
| How does `findByCategoryId` work without SQL? | Spring Data parses the method name into a query at startup. |
| What does `@Transactional` do? | Groups the DB operations; all roll back if an exception is thrown. |
| Why H2 in tests? | Fast, in-memory, no installation, isolated from real data. |
