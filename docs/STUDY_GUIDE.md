# CourseHub – Study Guide

Goal of this guide: after reading it you can explain **every file** of the project, justify every design choice, and answer the teacher's questions. Read it once with the code open next to it.

---

## 0. First run checklist (do this before anything else)

The project **has been compiled and run**: `mvn clean test` passes (5 tests) on JDK 22 and
JDK 27, and all 76 Postman scenarios were replayed against a running instance
(see `docs/TEST_EVIDENCE.md`). On your machine:

1. Install JDK 17+, Maven, MySQL. No IDE plugin is needed: the project does **not** use Lombok, so getters, setters and constructors are plain Java.
2. Run `mvn test` → this compiles everything and starts the whole Spring context on H2 (no MySQL needed).
3. Set `DB_USERNAME` / `DB_PASSWORD`, run `mvn spring-boot:run`.
4. Import the Postman collection and run the whole collection. All tests should be green.
5. Take your screenshots, push to GitHub, add `awadi.hatem@gmail.com` as collaborator.

> The build uses Java 17 language level and no annotation processors, so it compiles on
> **JDK 17 through JDK 27** and in any IDE without plugins.

If port 8080 is already used, add `--server.port=8081` and change the collection variable
`baseUrl`. If the Spring Boot version `3.5.0` in `pom.xml` is refused by your IDE, change it to
the latest 3.5.x.

---

## 1. The big picture

**Domain:** an online course platform. 7 entities:

```
Category 1───* Course *───1 Instructor
                 *
                 │ many-to-many (join table course_tags)
                 *
               Tag

Student 1───* Enrollment *───1 Course        (Enrollment = Student <-> Course + data)
Student 1───* Review     *───1 Course
```

**Architecture (4 layers, as in your Chapter 2):**

| Layer | Package | Job | Knows about |
|---|---|---|---|
| Controller | `controller` | Receives HTTP, converts JSON, validates, returns status code | Services only |
| Service | `service` | **Business rules**, 404/409/422 decisions, transactions | Repositories |
| Repository | `repository` | Talks to the database (Spring writes the SQL) | Entities |
| Model | `model` | Java classes mapped to tables | – |

**Walk-through of one request: `POST /api/enrollments` with `{"studentId":1,"courseId":2}`**

1. Tomcat gets the HTTP request; Spring MVC finds `EnrollmentController.create` because of `@PostMapping` on path `/api/enrollments`.
2. Jackson converts the JSON into an `Enrollment` (the entity). `@Valid` runs the validation annotations. If a field is invalid → exception → `GlobalExceptionHandler` → **400**.
3. The controller calls `enrollmentService.create(enrollment)`. It contains no logic.
4. The service loads the student and the course (`findById`; missing → `ResourceNotFoundException` → **404**), checks the student is not already enrolled (→ **409**), builds an `Enrollment` (progress 0, ACTIVE, now) and calls `save`.
5. Hibernate generates an `INSERT` (you see it in the console thanks to `show-sql=true`).
6. The saved entity goes back up; Jackson turns it into JSON; `@ResponseStatus(CREATED)` makes the status **201**.

---

## 2. What is new compared to the course slides

Your teacher's grid asks for things the slides do not show. Know these, they are likely oral questions.

| Topic | Course slides | This project | Why |
|---|---|---|---|
| Service layer | Drawn in the architecture, but code goes controller → repository | Real `@Service` classes hold the logic | The grid grades "business logic in services, not controllers" |
| Validation | Not covered | `@NotBlank`, `@Size`, `@Email`... + `@Valid` | Graded in entity modeling (4 pts) |
| Error handling | Not covered | Custom exceptions + `@RestControllerAdvice` | Correct HTTP codes |
| `@ManyToMany` | Only listed | `Course` ↔ `Tag` with a join table | Required relationship type |
| Injection | `@Autowired` on fields | Constructor injection with `private final` fields | Current recommended style, easier to test |
| `javax` | Slides mention `javax` | `jakarta.*` imports | Spring Boot 3 uses Jakarta EE (renamed from Java EE) |
| JSON loops | Not handled | `@JsonIgnore` on inverse collections | Avoids infinite recursion Course → Category → Courses → ... |
| Update verb | One slide shows PATCH | `PUT` everywhere | PUT = replace the whole resource, PATCH = partial. We use PUT consistently |
| Database | H2 then MySQL | MySQL (H2 only in tests) | Project requires MySQL or PostgreSQL |

---

## 3. File-by-file explanation

### 3.1 `pom.xml`

- `spring-boot-starter-parent`: gives compatible versions of every library, so dependencies have no version number.
- Starters (a *starter* is a bundle of dependencies + auto-configuration, Chapter 2):
  - `starter-web` → REST + embedded Tomcat + Jackson (JSON)
  - `starter-data-jpa` → JPA, Hibernate, Spring Data repositories
  - `starter-validation` → Bean Validation (`jakarta.validation`)
  - `mysql-connector-j` (runtime) → JDBC driver for MySQL
  - `starter-test` → JUnit, MockMvc
  - `h2` (runtime) → in-memory DB for the tests and the optional `demo` profile

### 3.2 `application.properties`

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/coursehub?createDatabaseIfNotExist=true&...}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

`${NAME:default}` means "use the environment variable NAME, otherwise the default". This is how we avoid committing the password to GitHub.

- `createDatabaseIfNotExist=true` → MySQL creates the `coursehub` schema for you.
- `ddl-auto=update` → Hibernate creates/alters tables to match the entities and **never deletes data** (`create` would wipe everything at each start).
- `show-sql=true` → prints the SQL; very useful to explain what JPA does.
- `open-in-view=false` → the database session closes at the end of the service, not during JSON writing. That is why relations used in JSON are `EAGER`.

### 3.3 Entities (`model` package)

Read `Category.java` first, it is the simplest. Every annotation:

| Annotation | Meaning |
|---|---|
| `@Entity` | This class is mapped to a table |
| `@Table(name="categories")` | Name of the table |
| `@Id` | Primary key |
| `@GeneratedValue(strategy = IDENTITY)` | MySQL `AUTO_INCREMENT` |
| `@Column(nullable=false, unique=true, length=80)` | Column constraints in the **database** |
| `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`, `@PositiveOrZero`, `@Past`, `@NotNull` | Validation rules checked **in Java** before the database is touched |
| `@Enumerated(EnumType.STRING)` | Store the enum as text ("BEGINNER") instead of 0/1/2 (safer if the enum order changes) |
| Getters/setters + no-arg constructor | Written explicitly (no Lombok, no annotation processing) |

> `@Column` protects the database; `@NotBlank/@Size...` protect the API and give nice error messages. We use both.

Every entity has explicit getters/setters and a public no-arg constructor (JPA requires it). We do **not** generate `toString`/`equals` on entities: the default object identity is what JPA needs, and a `toString` over bidirectional relations can loop forever.

#### Relationships (the most important part for the oral)

**One-to-many / many-to-one: `Category` ↔ `Course`**

```java
// In Course (the "many" side, OWNS the foreign key)
@ManyToOne(fetch = FetchType.EAGER, optional = false)
@JoinColumn(name = "category_id", nullable = false)
private Category category;

// In Category (the "one" side, inverse)
@OneToMany(mappedBy = "category")
@JsonIgnore
private List<Course> courses;
```

- The table `courses` has a `category_id` column (foreign key). That column is created by `@JoinColumn` on the `@ManyToOne` side.
- `mappedBy = "category"` says: "I do not own the relationship, the field `category` in `Course` does". No extra column in `categories`.
- Rule to remember (Chapter 3): **`@ManyToOne` side = owner, has `@JoinColumn`. `@OneToMany(mappedBy)` side = inverse.**
- `Instructor` ↔ `Course` works identically.

**Many-to-many: `Course` ↔ `Tag`**

```java
// Course = owning side
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "course_tags",
           joinColumns = @JoinColumn(name = "course_id"),
           inverseJoinColumns = @JoinColumn(name = "tag_id"))
private Set<Tag> tags = new HashSet<>();

// Tag = inverse side
@ManyToMany(mappedBy = "tags")
private Set<Course> courses;
```

- A many-to-many cannot be stored in one column, so a **join table** `course_tags(course_id, tag_id)` is created.
- `joinColumns` = the column pointing to *this* entity (Course). `inverseJoinColumns` = the column pointing to the *other* entity (Tag).
- `Set` instead of `List`: a course cannot have the same tag twice.

**Why is `Enrollment` an entity and not a `@ManyToMany` between Student and Course?** Because the link itself carries data (progress, status, date). A plain many-to-many has no room for extra columns. So we split it into two `@ManyToOne`: Enrollment → Student and Enrollment → Course. The same idea applies to `Review`.

**Unique constraints on the link tables:**

```java
@Table(name="enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","course_id"}))
```

The database itself forbids enrolling twice. The service also checks it first to return a friendly 409.

**`@JsonIgnore` on the inverse sides:** without it, serializing a Category writes its courses, each course writes its category, which writes its courses... → `StackOverflowError`. We cut the loop on the "many" collections. The `@ManyToOne` fields are still serialized, so a course shows its category, instructor and tags.

**EAGER vs LAZY:** LAZY loads a relation only when accessed; EAGER loads it immediately. We need category/instructor/tags when we write the JSON, and the DB session is closed by then (`open-in-view=false`), so these are EAGER. The big collections (courses of a category, enrollments...) stay LAZY and are never serialized.

### 3.4 Repositories

```java
public interface CategoryRepository extends JpaRepository<Category, Long> { ... }
```

Extending `JpaRepository<Entity, IdType>` gives you for free: `findAll`, `findById`, `save` (insert or update), `delete`, `existsById`, `count`, paging... You write **no implementation**; Spring generates it at startup.

**Derived queries**: Spring reads the method name and writes the SQL.

| Method | Meaning |
|---|---|
| `findByNameIgnoreCase(String)` | `WHERE LOWER(name) = LOWER(?)` |
| `findByCategoryId(Long)` | `WHERE category_id = ?` (property `category`, then its `id`) |
| `existsByStudentIdAndCourseId(a, b)` | Is there a row with both values? → boolean |
| `existsByStudentIdAndCourseIdAndStatusNot(a, b, s)` | same, and `status <> s` |
| `countByCourseId(Long)` | `SELECT COUNT(*) ... WHERE course_id = ?` |

### 3.5 The 5 custom queries (graded: "3 relevant custom queries")

They use **JPQL**: SQL written against *entities and fields* (`Course c`, `c.title`), not tables and columns.

**1. Catalogue search** – `CourseRepository.search`
```sql
SELECT c FROM Course c
WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.price <= :maxPrice
ORDER BY c.price ASC
```
Need: a visitor looks for "spring" courses under 100. `:keyword` is a named parameter bound by `@Param`.

**2. Class roster** – `StudentRepository.findStudentsOfCourse`
```sql
SELECT s FROM Student s JOIN s.enrollments e
WHERE e.course.id = :courseId AND e.status <> :excluded ORDER BY s.fullName
```
Need: the instructor wants the list of students (not cancelled). `JOIN s.enrollments` follows the mapped relationship; no table names, no ON clause.

**3. Popularity ranking** – `EnrollmentRepository.findMostPopularCourses`
```sql
SELECT c.id, c.title, COUNT(e)
FROM Enrollment e JOIN e.course c WHERE e.status <> :excluded
GROUP BY c.id, c.title ORDER BY COUNT(e) DESC, c.title ASC
```
Need: homepage "top courses". The query returns one `Object[]` row per course (`id`, `title`, count); the service turns each row into a `Map` with the same JSON keys (`courseId`, `title`, `enrollments`). The `Pageable` parameter (`PageRequest.of(0, limit)`) limits the number of rows (top N).

**4. Instructor revenue** – `EnrollmentRepository.sumRevenueByInstructor`
```sql
SELECT SUM(c.price) FROM Enrollment e JOIN e.course c
WHERE c.instructor.id = :instructorId AND e.status <> :excluded
```
Need: how much each instructor generated. `SUM` returns `null` when no row matches, so the service converts it to 0.

**5. Average rating** – `ReviewRepository.findAverageRatingByCourseId`
```sql
SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId
```
Need: show the quality of a course. Returns `null` if nobody reviewed it.

### 3.6 Services

Pattern in every service:

```java
@Service
@Transactional(readOnly = true)          // every method is a read-only transaction by default
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {   // constructor injection
        this.categoryRepository = categoryRepository;
    }
}

@Transactional                           // writing methods override with a normal transaction
public Category create(...) { ... }
```

- `@Service` → Spring creates one instance (a *bean*) and can inject it.
- **Constructor injection**: Spring sees the constructor needs a `CategoryRepository` and passes it. `final` fields + an explicit constructor = no `@Autowired` needed.
- `@Transactional`: all database operations of the method succeed together or are all rolled back if an exception is thrown. `readOnly = true` is an optimization for reads.
- `create` sets `setId(null)` so a client cannot overwrite an existing row by sending an id.
- `update` loads the existing entity, copies the fields, then saves (so JPA does an UPDATE, not an INSERT).
- The uniqueness helper `checkNameIsUnique(name, currentId)` ignores the entity being updated, so you can save a category without changing its name.

**All business rules** (this is what the "Service layer" grade looks for):

| Service | Rule | Result |
|---|---|---|
| Category / Instructor / Tag / Student | name/email must be unique | 409 |
| Category | cannot delete if it has courses | 409 |
| Instructor | cannot delete if he teaches courses | 409 |
| Student | cannot delete if he has enrollments or reviews | 409 |
| Course | cannot delete if it has enrollments or reviews | 409 |
| Course | unknown category / instructor / tag id | 404 |
| Tag | deleting detaches it from its courses first | 204 |
| Enrollment | one per student per course | 409 |
| Enrollment | cancelled enrollment is frozen | 422 |
| Enrollment | progress 100 ⇒ COMPLETED, COMPLETED ⇒ progress 100 | – |
| Review | student must be enrolled (not cancelled) | 422 |
| Review | one review per student per course | 409 |

### 3.7 Controllers

```java
@RestController                  // @Controller + @ResponseBody: return values become JSON
@RequestMapping("/api/categories")
public class CategoryController {
    @GetMapping("/{id}")                         // GET /api/categories/5
    public Category getById(@PathVariable Long id) { ... }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)          // 201
    public Category create(@Valid @RequestBody Category category) { ... }
}
```

- `@PathVariable` takes `{id}` from the URL. `@RequestBody` converts the JSON body into a Java object. `@RequestParam` reads `?keyword=...`.
- `@Valid` triggers the validation annotations of the object; if one fails, Spring throws `MethodArgumentNotValidException` **before** the method runs.
- Order of mappings: `/search` and `/popular` are literal paths, so Spring prefers them over `/{id}`.

| Operation | Verb | Success code |
|---|---|---|
| Read | GET | 200 |
| Create | POST | 201 |
| Update | PUT | 200 |
| Delete | DELETE | 204 (no body) |

### 3.8 Request bodies and statistics (no separate DTO package)

There is no `dto` package. The entities are used directly as `@RequestBody`, and the JSON
still names related rows by id (`categoryId`, `instructorId`, `tagIds`, `studentId`,
`courseId`) exactly as before.

- A small **link method** on the entity converts each id into a lightweight reference
  (`linkCategory`, `linkInstructor`, `linkTags`, `linkStudent`, `linkCourse`). It is annotated
  `@JsonProperty(access = WRITE_ONLY)`, so the id is accepted in a request but never appears
  in a response (the response shows the real related object, as it did before).
- The **service** then loads the real row by id, replaces the reference, and answers 404 if
  the id does not exist. This keeps the controller thin and the rule in one place.
- The update endpoints for `Enrollment` and `Review` also take the entity; only `progress` /
  `status` or `rating` / `comment` are read, and the service checks those values.
- The statistics endpoints (`popular`, `rating`, `revenue`) build a `LinkedHashMap` with the
  aggregate values instead of a response class.

### 3.9 Exceptions (`exception` package)

| Exception | Thrown when | HTTP |
|---|---|---|
| `ResourceNotFoundException` | an id does not exist | 404 |
| `ConflictException` | duplicate, or delete blocked | 409 |
| `BusinessRuleException` | rule forbids the action | 422 |
| (Spring) `MethodArgumentNotValidException` | `@Valid` failed | 400 + `fieldErrors` |
| (Spring) `HttpMessageNotReadableException` | broken JSON / bad enum | 400 |
| (Spring) `MethodArgumentTypeMismatchException` | `/courses/abc` | 400 |
| (Spring) `DataIntegrityViolationException` | DB constraint violated | 409 (safety net) |
| anything else | bug | 500 |

`@RestControllerAdvice` on `GlobalExceptionHandler` = "this class handles exceptions for **all** controllers". Each `@ExceptionHandler(X.class)` method catches exception X and builds a response using the same `ApiError` shape. The services and controllers just `throw`, they never build error responses.

**400 vs 404 vs 409 vs 422 in one sentence each:** 400 = the request is malformed or invalid; 404 = the thing you asked for does not exist; 409 = the request conflicts with the current data (duplicate, still referenced); 422 = the request is valid but a business rule refuses it.

### 3.10 Tests

- `CoursehubApplicationTests.contextLoads` starts the full Spring context. If an entity mapping or a JPQL query is wrong, this test fails, so it checks all 5 custom queries' syntax too.
- `CategoryApiTests` uses **MockMvc** (calls the controllers without a real server) to check 201, 400, 409 and 404.
- Both use `@ActiveProfiles("test")` → loads `application-test.properties` → in-memory H2, so no MySQL is needed.
- `@Transactional` on the test class rolls back each test.
- The Postman collection is the main functional test (76 requests).

---

## 4. Postman collection

Import `postman/CourseHub.postman_collection.json`, then **Run collection**.

| Folder | Content |
|---|---|
| 1 Create | creates a full dataset and stores each id in a collection variable (`categoryId`, `courseId`...) |
| 2 Read | all GETs |
| 3 Update | all PUTs |
| 4 Custom queries | the 5 queries + 3 derived ones, with assertions |
| 5 Error cases | 400, 404, 409, 422 (graded: "error cases") |
| 6 Cleanup | DELETE everything, then checks 404 |

The `suffix` variable (a timestamp) makes names/emails unique so you can run it many times. Screenshots to take: the Runner summary (all green), one 201, one 400 with `fieldErrors`, one 404, one 409.

---

## 5. The 5-minute presentation (suggested script)

1. **(30 s) Context.** "CourseHub is the backend of an online learning platform: instructors publish courses, students enroll and review them. 7 entities."
2. **(60 s) Data model.** Show the ER diagram from the README. Name the 3 relationship types: one-to-many (Category–Course), many-to-many (Course–Tag, join table), and the association entities (Enrollment, Review) that carry extra data.
3. **(60 s) Architecture.** "Controller → Service → Repository → MySQL. Controllers only handle HTTP, services hold business rules, repositories use Spring Data." Give one rule as an example: cannot delete a category that still has courses (409).
4. **(60 s) Custom queries.** Show 2–3: search, popularity ranking (`GROUP BY`), revenue.
5. **(60 s) Validation and errors.** Show a failing POST in Postman (400 with `fieldErrors`) and explain `@Valid` + `@RestControllerAdvice`.
6. **(30 s) Demo.** Postman Runner, all green.

Each team member should be ready to take any section.

---

## 6. Questions the teacher may ask

1. **Why a service layer?** Controllers handle HTTP only; services hold business rules and transactions; this makes each layer replaceable and testable.
2. **What does `@RestController` do?** `@Controller` + `@ResponseBody`: methods return objects that are converted to JSON.
3. **What is dependency injection? Why constructor injection?** Spring creates the objects and passes them in. Constructor injection makes dependencies explicit and `final`, and lets you create the class in a test without Spring.
4. **How do you inject dependencies without Lombok?** The class declares `private final` fields and an explicit constructor; Spring calls that constructor with the beans it found.
5. **Owning side vs `mappedBy`?** The owning side holds the foreign key (`@JoinColumn` / `@JoinTable`); `mappedBy` marks the inverse side that just mirrors it.
6. **Why is Enrollment an entity?** The relation has its own data (progress, status, date) that a plain `@ManyToMany` cannot store.
7. **What is the join table?** `course_tags(course_id, tag_id)`, created by `@JoinTable` on Course.
8. **Why `@JsonIgnore`?** To break the infinite loop when serializing bidirectional relations.
9. **EAGER vs LAZY?** Eager loads the relation with the entity; lazy loads it on first access. Lazy needs an open session.
10. **What does `ddl-auto=update` do vs `create`?** `update` adjusts the schema and keeps data; `create` drops and recreates everything at each startup.
11. **What does `@Transactional` do?** Groups the database operations; rolls back all if an exception occurs.
12. **How does `findByCategoryId` work without SQL?** Spring Data parses the method name into a query at startup.
13. **JPQL vs SQL?** JPQL works on entities/fields; Hibernate translates it to SQL for the database.
14. **How does the popularity query return its rows?** It selects `c.id`, `c.title` and `COUNT(e)`; the service maps each row to a `Map` (`courseId`, `title`, `enrollments`).
15. **How are related rows sent without DTOs?** The client sends ids; a write-only link method on the entity builds a reference and the service loads the real row (404 if unknown).
16. **`@Valid` vs the annotations on the entity?** The annotations declare the rules; `@Valid` on the controller parameter triggers them. Hibernate also re-checks entities before saving.
17. **Why PUT and not PATCH?** PUT replaces the whole resource, PATCH changes part of it. We use PUT for full updates (the enrollment PUT accepts optional fields, so it behaves partially; say this honestly if asked).
18. **Why env variables for the DB password?** Secrets must not be committed to Git.
19. **`javax` vs `jakarta`?** Java EE moved to the Eclipse Foundation and was renamed Jakarta EE; Spring Boot 3 uses `jakarta.*`.
20. **What happens when you delete a tag used by courses?** `TagService.delete` first removes the tag from each course's `tags` set (Course owns the join table), then deletes the tag.
21. **Why `setId(null)` in `create`?** To prevent a client from choosing/overwriting an id.
22. **Why H2 for tests?** Fast, in memory, no installation, isolated from real data.

---

## 7. Honest limits (good to know before someone asks)

- No authentication/authorization (not requested; Chapter 4+ material).
- No pagination on list endpoints (`findAll`); fine for a mini project.
- Entities are returned directly for the simple resources; a bigger project would use a dedicated response model everywhere.
- Money uses `BigDecimal` (correct), but there is no payment model: "revenue" is the sum of course prices of non-cancelled enrollments.
