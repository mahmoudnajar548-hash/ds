# Presentation

A 10-slide, 5-minute deck for the DS1 defence.

| File | What it is |
|---|---|
| `CourseHub_DS1_Presentation.pptx` | the deck (16:9, speaker notes on every slide) |
| `SPEAKER_SCRIPT.md` | the exact 5-minute script with timings |
| `images/` | rendered screenshots of the real source files + diagrams |
| `preview/` | one PNG per slide (exported from the PPTX) |
| `build_deck.py` | regenerates the images and the deck |
| `assets/validator-output.txt` | the captured 76/76 API run used on slide 9 |

## What is on each slide

1. **Title** — project, stack, name, date
2. **Overview** — 7 entities, 40+ endpoints, 76 checks
3. **Architecture** — client → controller → service → repository → database
4. **Data model** — ER diagram, one-to-many / many-to-many / association entities
5. **JPA mapping** — `Course.java` relationships (`@ManyToOne`, `@ManyToMany`, `@JoinTable`)
6. **Business rules** — `EnrollmentService` (409, 422, 100% → COMPLETED)
7. **Custom queries** — `CourseRepository.search` and `EnrollmentRepository` aggregates
8. **Validation and errors** — `GlobalExceptionHandler` + a real 400 JSON body
9. **Testing** — 76/76 terminal evidence + metrics
10. **Wrap-up** — demo path, what it demonstrates, GitHub link

The code images are real excerpts: the build script reads the files and line ranges, so they
stay in sync with the source.

## Change the name / a text

The presenter name is on slide 1 (`Mahmoud Najar`). The easiest way is to edit it directly in
PowerPoint. If you regenerate the deck, change it in `build_deck.py` (`text(s, "Mahmoud Najar"`).

## Regenerate

Requires Python 3 with `python-pptx`, `Pygments` and `Pillow`:

```powershell
python -m pip install python-pptx pygments pillow
python docs/presentation/build_deck.py
```

## Export a PDF (optional)

Open the PPTX in PowerPoint → **File → Save As → PDF**. The `preview/` folder already contains
one PNG per slide if you need images for a report.
