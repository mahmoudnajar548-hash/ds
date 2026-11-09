# End-to-end API validation (no Postman CLI required).
#
# Replays the same 76 scenarios as postman/CourseHub.postman_collection.json over real HTTP,
# so the results can be reproduced from a terminal or a CI job.
#
# Usage:
#   pwsh -File scripts/validate-api.ps1                       # http://localhost:8081
#   pwsh -File scripts/validate-api.ps1 -BaseUrl http://localhost:8080
#
# Exit code 0 = all checks passed, 1 = at least one check failed.

param(
    [string]$BaseUrl = 'http://localhost:8081'
)

$ErrorActionPreference = 'Stop'
$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()
$results = New-Object System.Collections.Generic.List[object]

function Call {
    param([string]$Method, [string]$Path, [string]$Body)
    $params = @{ Uri = "$BaseUrl$Path"; Method = $Method; SkipHttpErrorCheck = $true; TimeoutSec = 30 }
    if ($Body) { $params.Body = $Body; $params.ContentType = 'application/json' }
    $r = Invoke-WebRequest @params
    $json = $null
    if ($r.Content) { try { $json = $r.Content | ConvertFrom-Json } catch { } }
    return [pscustomobject]@{ Status = [int]$r.StatusCode; Json = $json; Raw = $r.Content }
}

function Check {
    param([string]$Name, [int]$Expected, $Response, [scriptblock]$Assert)
    $ok = ($Response.Status -eq $Expected)
    $detail = ''
    if ($ok -and $Assert) {
        try { $ok = [bool](& $Assert $Response.Json) }
        catch { $ok = $false; $detail = "assertion error: $($_.Exception.Message)" }
    }
    $results.Add([pscustomobject]@{
        Test = $Name; Expected = $Expected; Actual = $Response.Status; Pass = $ok; Detail = $detail
    })
}

# ---------- 1. CREATE (happy path) ----------
$r = Call POST '/api/categories' "{`"name`":`"Programming $suffix`",`"description`":`"Code courses`"}"
Check 'POST /api/categories -> 201' 201 $r { param($j) $j.name -like 'Programming*' }
$categoryId = $r.Json.id

$r = Call POST '/api/instructors' "{`"firstName`":`"Hatem`",`"lastName`":`"Awadi $suffix`",`"email`":`"hatem.$suffix@example.com`",`"bio`":`"Spring trainer`"}"
Check 'POST /api/instructors -> 201' 201 $r { param($j) $j.email -like 'hatem.*' }
$instructorId = $r.Json.id

$r = Call POST '/api/tags' "{`"name`":`"java-$suffix`"}"
Check 'POST /api/tags (java) -> 201' 201 $r
$tagId1 = $r.Json.id

$r = Call POST '/api/tags' "{`"name`":`"spring-$suffix`"}"
Check 'POST /api/tags (spring) -> 201' 201 $r
$tagId2 = $r.Json.id

$r = Call POST '/api/courses' "{`"title`":`"Spring Boot REST APIs $suffix`",`"description`":`"Build a complete REST API`",`"price`":49.99,`"durationHours`":12,`"level`":`"INTERMEDIATE`",`"categoryId`":$categoryId,`"instructorId`":$instructorId,`"tagIds`":[$tagId1,$tagId2]}"
Check 'POST /api/courses -> 201, relations resolved' 201 $r {
    param($j) $j.category.id -eq $categoryId -and $j.instructor.id -eq $instructorId -and $j.tags.Count -eq 2
}
$courseId = $r.Json.id

$r = Call POST '/api/students' "{`"fullName`":`"Student A $suffix`",`"email`":`"studenta.$suffix@example.com`",`"birthDate`":`"2000-05-10`"}"
Check 'POST /api/students -> 201' 201 $r
$studentA = $r.Json.id

$r = Call POST '/api/enrollments' "{`"studentId`":$studentA,`"courseId`":$courseId}"
Check 'POST /api/enrollments -> 201' 201 $r { param($j) $j.progress -eq 0 -and $j.status -eq 'ACTIVE' }
$enrollmentA = $r.Json.id

$r = Call PUT "/api/enrollments/$enrollmentA" '{ "progress": 50 }'
Check 'PUT /api/enrollments/{id} progress 50 -> 200' 200 $r { param($j) $j.progress -eq 50 -and $j.status -eq 'ACTIVE' }

$r = Call POST '/api/reviews' "{`"studentId`":$studentA,`"courseId`":$courseId,`"rating`":5,`"comment`":`"Excellent`"}"
Check 'POST /api/reviews -> 201' 201 $r { param($j) $j.rating -eq 5 }
$reviewId = $r.Json.id

# ---------- 2. READ ----------
foreach ($path in @('/api/categories','/api/instructors','/api/tags','/api/students','/api/courses','/api/enrollments','/api/reviews')) {
    $r = Call GET $path
    Check "GET $path -> 200 non-empty" 200 $r { param($j) $j.Count -ge 1 }
}
foreach ($pair in @(@('/api/categories',$categoryId),@('/api/instructors',$instructorId),@('/api/tags',$tagId1),@('/api/students',$studentA),@('/api/courses',$courseId),@('/api/enrollments',$enrollmentA),@('/api/reviews',$reviewId))) {
    $r = Call GET "$($pair[0])/$($pair[1])"
    Check "GET $($pair[0])/{id} -> 200" 200 $r { param($j) $j.id -eq $pair[1] }
}

# ---------- 3. UPDATE ----------
$r = Call PUT "/api/categories/$categoryId" "{`"name`":`"Programming $suffix`",`"description`":`"Updated description`"}"
Check 'PUT /api/categories/{id} -> 200' 200 $r { param($j) $j.description -eq 'Updated description' }

$r = Call PUT "/api/instructors/$instructorId" "{`"firstName`":`"Hatem`",`"lastName`":`"Awadi $suffix`",`"email`":`"hatem.$suffix@example.com`",`"bio`":`"Updated bio`"}"
Check 'PUT /api/instructors/{id} -> 200' 200 $r { param($j) $j.bio -eq 'Updated bio' }

$r = Call PUT "/api/tags/$tagId1" "{`"name`":`"java-core-$suffix`"}"
Check 'PUT /api/tags/{id} -> 200' 200 $r { param($j) $j.name -eq "java-core-$suffix" }

$r = Call PUT "/api/students/$studentA" "{`"fullName`":`"Student A Updated $suffix`",`"email`":`"studenta.$suffix@example.com`",`"birthDate`":`"2000-05-10`"}"
Check 'PUT /api/students/{id} -> 200' 200 $r { param($j) $j.fullName -like 'Student A Updated*' }

$r = Call PUT "/api/courses/$courseId" "{`"title`":`"Spring Boot REST APIs $suffix`",`"description`":`"Updated course`",`"price`":39.99,`"durationHours`":12,`"level`":`"ADVANCED`",`"categoryId`":$categoryId,`"instructorId`":$instructorId,`"tagIds`":[$tagId1,$tagId2]}"
Check 'PUT /api/courses/{id} -> 200 (new price)' 200 $r { param($j) [decimal]$j.price -eq 39.99 -and $j.level -eq 'ADVANCED' }

$r = Call PUT "/api/reviews/$reviewId" '{ "rating": 4, "comment": "Very good" }'
Check 'PUT /api/reviews/{id} -> 200 (rating 4)' 200 $r { param($j) $j.rating -eq 4 }

# ---------- 4. CUSTOM QUERIES ----------
$r = Call GET "/api/courses/search?keyword=Spring&maxPrice=100"
Check 'Q1 search keyword+price -> 200 contains course' 200 $r { param($j) ($j | Where-Object { $_.id -eq $courseId }).Count -eq 1 }

$r = Call GET "/api/courses/search?keyword=Spring&maxPrice=10"
Check 'Q1b search budget too low -> 200 empty' 200 $r { param($j) $j.Count -eq 0 }

$r = Call GET "/api/courses/$courseId/students"
Check 'Q2 class roster -> 200 contains student' 200 $r { param($j) ($j | Where-Object { $_.id -eq $studentA }).Count -eq 1 }

$r = Call GET '/api/courses/popular?limit=3'
Check 'Q3 popular courses -> 200 sorted desc' 200 $r {
    param($j)
    if ($j.Count -lt 1) { return $false }
    for ($i = 1; $i -lt $j.Count; $i++) { if ($j[$i-1].enrollments -lt $j[$i].enrollments) { return $false } }
    return $true
}

$r = Call GET "/api/instructors/$instructorId/revenue"
Check 'Q4 instructor revenue -> 39.99' 200 $r { param($j) [decimal]$j.totalRevenue -eq 39.99 }

$r = Call GET "/api/courses/$courseId/rating"
Check 'Q5 course average rating -> 4.0' 200 $r { param($j) $j.averageRating -eq 4 -and $j.reviewCount -eq 1 }

$r = Call GET "/api/courses/category/$categoryId"
Check 'Q6 courses of a category -> 200 contains course' 200 $r { param($j) ($j | Where-Object { $_.id -eq $courseId }).Count -eq 1 }

$r = Call GET "/api/enrollments/student/$studentA"
Check 'Q7 enrollments of a student -> 200 contains enrollment' 200 $r { param($j) ($j | Where-Object { $_.id -eq $enrollmentA }).Count -eq 1 }

$r = Call GET "/api/reviews/course/$courseId"
Check 'Q8 reviews of a course -> 200 contains review' 200 $r { param($j) ($j | Where-Object { $_.id -eq $reviewId }).Count -eq 1 }

# ---------- 5. ERROR CASES ----------
$r = Call GET '/api/categories/999999'
Check 'E01 404 unknown category' 404 $r

$r = Call POST '/api/courses' "{`"title`":`"Ghost course`",`"price`":10,`"durationHours`":5,`"level`":`"BEGINNER`",`"categoryId`":999999,`"instructorId`":$instructorId}"
Check 'E02 404 course with unknown category' 404 $r

$r = Call GET '/api/does-not-exist'
Check 'E03 404 unknown URL' 404 $r

$r = Call POST '/api/categories' '{ "name": "" }'
Check 'E04 400 blank category name + fieldErrors' 400 $r { param($j) $null -ne $j.fieldErrors.name }

$r = Call POST '/api/courses' "{`"title`":`"ab`",`"price`":-5,`"durationHours`":0,`"level`":`"BEGINNER`",`"categoryId`":$categoryId,`"instructorId`":$instructorId}"
Check 'E05 400 invalid course (3 field errors)' 400 $r {
    param($j)
    $keys = $j.fieldErrors.PSObject.Properties.Name | Sort-Object
    ($keys -join ',') -eq 'durationHours,price,title'
}

$r = Call POST '/api/courses' "{`"title`":`"Valid title`",`"price`":10,`"durationHours`":5,`"level`":`"EXPERT`",`"categoryId`":$categoryId,`"instructorId`":$instructorId}"
Check 'E06 400 invalid enum level=EXPERT' 400 $r

$r = Call POST '/api/categories' '{ "name": "broken json" '
Check 'E07 400 malformed JSON' 400 $r

$r = Call POST '/api/students' '{ "fullName": "Bad Email", "email": "not-an-email" }'
Check 'E08 400 invalid email' 400 $r { param($j) $null -ne $j.fieldErrors.email }

$r = Call GET '/api/courses/abc'
Check 'E09 400 id is not a number' 400 $r

$r = Call POST '/api/reviews' "{`"studentId`":$studentA,`"courseId`":$courseId,`"rating`":9}"
Check 'E10 400 review rating out of range' 400 $r

$r = Call POST '/api/categories' "{`"name`":`"Programming $suffix`"}"
Check 'E11 409 duplicate category name' 409 $r

$r = Call POST '/api/students' "{`"fullName`":`"Duplicate`",`"email`":`"studenta.$suffix@example.com`"}"
Check 'E12 409 duplicate student email' 409 $r

$r = Call POST '/api/enrollments' "{`"studentId`":$studentA,`"courseId`":$courseId}"
Check 'E13 409 student already enrolled' 409 $r

$r = Call POST '/api/reviews' "{`"studentId`":$studentA,`"courseId`":$courseId,`"rating`":3}"
Check 'E14 409 student already reviewed this course' 409 $r

$r = Call DELETE "/api/categories/$categoryId"
Check 'E15 409 cannot delete category that has courses' 409 $r

$r = Call DELETE "/api/instructors/$instructorId"
Check 'E16 409 cannot delete instructor that has courses' 409 $r

$r = Call DELETE "/api/courses/$courseId"
Check 'E17 409 cannot delete course that has enrollments' 409 $r

$r = Call DELETE "/api/students/$studentA"
Check 'E18 409 cannot delete student that has enrollments' 409 $r

$r = Call POST '/api/students' "{`"fullName`":`"Student B $suffix`",`"email`":`"studentb.$suffix@example.com`",`"birthDate`":`"1999-01-01`"}"
Check 'E19 (setup) create second student -> 201' 201 $r
$studentB = $r.Json.id

$r = Call POST '/api/reviews' "{`"studentId`":$studentB,`"courseId`":$courseId,`"rating`":3}"
Check 'E20 422 review by a non-enrolled student' 422 $r

$r = Call POST '/api/enrollments' "{`"studentId`":$studentB,`"courseId`":$courseId}"
Check 'E21 (setup) enroll second student -> 201' 201 $r
$enrollmentB = $r.Json.id

$r = Call PUT "/api/enrollments/$enrollmentB" '{ "status": "CANCELLED" }'
Check 'E22 (setup) cancel enrollment -> 200' 200 $r { param($j) $j.status -eq 'CANCELLED' }

$r = Call PUT "/api/enrollments/$enrollmentB" '{ "progress": 10 }'
Check 'E23 422 cancelled enrollment cannot be modified' 422 $r

$r = Call GET "/api/courses/$courseId/students"
Check 'E24 cancelled student is not in the roster' 200 $r { param($j) ($j | Where-Object { $_.id -eq $studentB }).Count -eq 0 }

$r = Call PUT "/api/enrollments/$enrollmentA" '{ "progress": 100 }'
Check 'E25 100% progress auto-completes enrollment' 200 $r { param($j) $j.status -eq 'COMPLETED' -and $j.progress -eq 100 }

# ---------- 6. DELETE AND CLEANUP ----------
$r = Call DELETE "/api/tags/$tagId2"
Check 'DELETE /api/tags/{id} (detached first) -> 204' 204 $r

$r = Call GET "/api/courses/$courseId"
Check 'Course now has only 1 tag' 200 $r { param($j) $j.tags.Count -eq 1 }

$r = Call DELETE "/api/reviews/$reviewId"
Check 'DELETE /api/reviews/{id} -> 204' 204 $r

$r = Call DELETE "/api/enrollments/$enrollmentB"
Check 'DELETE enrollment B -> 204' 204 $r

$r = Call DELETE "/api/enrollments/$enrollmentA"
Check 'DELETE enrollment A -> 204' 204 $r

$r = Call DELETE "/api/students/$studentB"
Check 'DELETE student B -> 204' 204 $r

$r = Call DELETE "/api/students/$studentA"
Check 'DELETE student A -> 204' 204 $r

$r = Call DELETE "/api/courses/$courseId"
Check 'DELETE course -> 204' 204 $r

$r = Call DELETE "/api/tags/$tagId1"
Check 'DELETE tag 1 -> 204' 204 $r

$r = Call DELETE "/api/instructors/$instructorId"
Check 'DELETE instructor -> 204' 204 $r

$r = Call DELETE "/api/categories/$categoryId"
Check 'DELETE category -> 204' 204 $r

$r = Call GET "/api/courses/$courseId"
Check 'Course is really gone -> 404' 404 $r

$r = Call DELETE "/api/courses/$courseId"
Check 'Deleting again -> 404' 404 $r

# ---------- report ----------
$pass = ($results | Where-Object { $_.Pass }).Count
$fail = $results.Count - $pass
$results | ForEach-Object {
    $flag = if ($_.Pass) { 'PASS' } else { 'FAIL' }
    "{0} | {1} | expected {2} got {3} {4}" -f $flag, $_.Test, $_.Expected, $_.Actual, $_.Detail
}
""
"TOTAL=$($results.Count) PASS=$pass FAIL=$fail"
if ($fail -gt 0) { exit 1 }
