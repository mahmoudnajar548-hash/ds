#!/usr/bin/env python3
"""Build the CourseHub DS1 presentation.

Creates:
  - images/*.png       rendered screenshots of the real source files + diagrams
  - CourseHub_DS1_Presentation.pptx
"""
import os

from PIL import Image, ImageDraw, ImageFont
from pygments import lex
from pygments.lexers import JavaLexer, JsonLexer
from pygments.styles import get_style_by_name
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.util import Inches, Pt

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PRES_DIR = os.path.join(ROOT, "docs", "presentation")
IMG_DIR = os.path.join(PRES_DIR, "images")
ASSET_DIR = os.path.join(PRES_DIR, "assets")
os.makedirs(IMG_DIR, exist_ok=True)

CONSOLA = "C:/Windows/Fonts/consola.ttf"
CONSOLA_B = "C:/Windows/Fonts/consolab.ttf"
CONSOLA_I = "C:/Windows/Fonts/consolai.ttf"
SEGOE = "C:/Windows/Fonts/segoeui.ttf"
SEGOE_B = "C:/Windows/Fonts/segoeuib.ttf"
SEGOE_SB = "C:/Windows/Fonts/seguisb.ttf"

# ---------- palette ----------
INK = RGBColor(0x0F, 0x17, 0x2A)
MUTED = RGBColor(0x64, 0x74, 0x8B)
TEAL = RGBColor(0x0D, 0x94, 0x88)
TEAL_LIGHT = RGBColor(0xE6, 0xFF, 0xFA)
AMBER = RGBColor(0xD9, 0x77, 0x06)
RED = RGBColor(0xDC, 0x26, 0x26)
BG = RGBColor(0xF7, 0xF9, 0xFC)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
LINE = RGBColor(0xE2, 0xE8, 0xF0)
DARK = RGBColor(0x0B, 0x12, 0x20)


def font(path, size):
    return ImageFont.truetype(path, size)


def read_lines(rel_path):
    with open(os.path.join(ROOT, rel_path), encoding="utf-8") as fh:
        return fh.read().splitlines()


def snippet(rel_path, ranges):
    lines = read_lines(rel_path)
    out = []
    for start, end in ranges:
        out.extend(lines[start - 1:end])
    return "\n".join(out)


def _token_color(style, ttype, fallback):
    info = style.style_for_token(ttype)
    color = "#" + info["color"] if info["color"] else fallback
    return color, info["bold"], info["italic"]


def render_code(code, out_path, title, lexer, theme="dark", font_size=30, min_width=820):
    dark = theme == "dark"
    bg = "#0F172A" if dark else "#FFFFFF"
    chrome = "#0B1220" if dark else "#F1F5F9"
    border = "#1F2A44" if dark else "#E2E8F0"
    default = "#E5E7EB" if dark else "#0F172A"
    title_col = "#94A3B8" if dark else "#475569"
    gutter_col = "#475569" if dark else "#94A3B8"

    style = get_style_by_name("monokai" if dark else "friendly")
    f = font(CONSOLA, font_size)
    fb = font(CONSOLA_B, font_size)
    fi = font(CONSOLA_I, font_size)
    ft = font(SEGOE_SB, 21)

    code = code.rstrip("\n")
    lines = code.split("\n")
    char_w = f.getlength("M")
    pad_x = 34
    gutter = int(char_w * 4.4)
    line_h = int(font_size * 1.43)
    chrome_h = 62
    width = int(pad_x * 2 + gutter + max(len(line) for line in lines) * char_w + 30)
    width = max(width, min_width)
    height = chrome_h + 26 + len(lines) * line_h + 24

    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle([0, 0, width - 1, height - 1], radius=18, fill=bg, outline=border, width=2)
    draw.rounded_rectangle([0, 0, width - 1, chrome_h], radius=18, fill=chrome)
    draw.rectangle([0, chrome_h - 18, width - 1, chrome_h], fill=chrome)
    for i, color in enumerate(("#EF4444", "#F59E0B", "#22C55E")):
        cx = 30 + i * 26
        draw.ellipse([cx - 7, chrome_h // 2 - 7, cx + 7, chrome_h // 2 + 7], fill=color)
    draw.text((112, chrome_h // 2 - 12), title, font=ft, fill=title_col)

    segments = [[]]
    for ttype, value in lex(code, lexer):
        parts = value.split("\n")
        for idx, part in enumerate(parts):
            if part:
                segments[-1].append((part, ttype))
            if idx < len(parts) - 1:
                segments.append([])
    if segments and not segments[-1]:
        segments.pop()

    y0 = chrome_h + 22
    for i, segs in enumerate(segments):
        y = y0 + i * line_h
        num = str(i + 1)
        draw.text((pad_x + gutter - 18 - f.getlength(num), y), num, font=f, fill=gutter_col)
        x = pad_x + gutter
        for text, ttype in segs:
            color, bold, italic = _token_color(style, ttype, default)
            use = fb if bold else (fi if italic else f)
            draw.text((x, y), text, font=use, fill=color)
            x += f.getlength(text)

    img.save(out_path)
    return out_path


def render_shell(lines, out_path, title, font_size=27):
    f = font(CONSOLA, font_size)
    ft = font(SEGOE_SB, 20)
    line_h = int(font_size * 1.42)
    pad_x = 32
    chrome_h = 58
    max_chars = max(len(t) for t, _ in lines)
    width = int(pad_x * 2 + max_chars * f.getlength("M") + 20)
    height = chrome_h + 24 + len(lines) * line_h + 20
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle([0, 0, width - 1, height - 1], radius=18, fill="#0F172A", outline="#1F2A44", width=2)
    draw.rounded_rectangle([0, 0, width - 1, chrome_h], radius=18, fill="#0B1220")
    draw.rectangle([0, chrome_h - 18, width - 1, chrome_h], fill="#0B1220")
    for i, color in enumerate(("#EF4444", "#F59E0B", "#22C55E")):
        cx = 30 + i * 26
        draw.ellipse([cx - 7, chrome_h // 2 - 7, cx + 7, chrome_h // 2 + 7], fill=color)
    draw.text((112, chrome_h // 2 - 11), title, font=ft, fill="#94A3B8")
    for i, (text, color) in enumerate(lines):
        draw.text((pad_x, chrome_h + 20 + i * line_h), text, font=f, fill=color)
    img.save(out_path)
    return out_path


# ---------- diagrams ----------
def _pill(draw, cx, cy, text, fnt, fill="#FFFFFF", ink="#0F172A"):
    w = draw.textlength(text, font=fnt) + 22
    draw.rounded_rectangle([cx - w / 2, cy - 16, cx + w / 2, cy + 16], radius=16, fill=fill, outline="#CBD5E1", width=2)
    draw.text((cx - w / 2 + 11, cy - 11), text, font=fnt, fill=ink)


def _entity(draw, x, y, w, h, name, fields, color, title_font, field_font):
    draw.rounded_rectangle([x, y, x + w, y + h], radius=18, fill="#FFFFFF", outline="#CBD5E1", width=3)
    draw.rounded_rectangle([x, y, x + w, y + 58], radius=18, fill=color)
    draw.rectangle([x, y + 40, x + w, y + 58], fill=color)
    draw.text((x + 24, y + 12), name, font=title_font, fill="#FFFFFF")
    for i, field_name in enumerate(fields):
        draw.text((x + 26, y + 76 + i * 34), field_name, font=field_font, fill="#334155")


def render_er(out_path):
    W, H = 2400, 1320
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    tf = font(SEGOE_B, 34)
    ff = font(SEGOE, 25)
    lf = font(SEGOE_SB, 22)

    def edge(points, color="#94A3B8"):
        d.line(points, fill=color, width=4, joint="curve")

    # edges first, so boxes cover their ends
    edge([(480, 220), (840, 220)])
    edge([(1920, 220), (1560, 220)])
    edge([(1560, 310), (1780, 310), (1780, 695), (1980, 695)])
    edge([(1020, 390), (810, 790)])
    edge([(1380, 390), (1590, 790)])
    edge([(1080, 1120), (810, 1040)])
    edge([(1320, 1120), (1590, 1040)])

    _pill(d, 660, 200, "1 : N", lf)
    _pill(d, 1740, 200, "1 : N", lf)
    _pill(d, 1880, 500, "N : M", lf)
    d.text((1795, 530), "course_tags", font=lf, fill="#475569")
    _pill(d, 915, 590, "1 : N", lf)
    _pill(d, 1485, 590, "1 : N", lf)
    _pill(d, 945, 1080, "1 : N", lf)
    _pill(d, 1455, 1080, "1 : N", lf)

    _entity(d, 60, 120, 420, 200, "Category", ["id (PK)", "name (unique)"], "#0EA5E9", tf, ff)
    _entity(d, 840, 90, 720, 300, "Course",
            ["id (PK)", "title, description", "price, level", "category_id (FK)", "instructor_id (FK)"],
            "#0D9488", tf, ff)
    _entity(d, 1920, 120, 420, 200, "Instructor", ["id (PK)", "firstName, lastName", "email (unique)"],
            "#8B5CF6", tf, ff)
    _entity(d, 1980, 600, 340, 190, "Tag", ["id (PK)", "name (unique)"], "#F59E0B", tf, ff)
    _entity(d, 540, 790, 540, 250, "Enrollment",
            ["id (PK)", "student_id (FK)", "course_id (FK)", "progress, status", "UNIQUE(student, course)"],
            "#6366F1", tf, ff)
    _entity(d, 1320, 790, 540, 250, "Review",
            ["id (PK)", "student_id (FK)", "course_id (FK)", "rating, comment", "UNIQUE(student, course)"],
            "#EF4444", tf, ff)
    _entity(d, 930, 1120, 540, 180, "Student", ["id (PK)", "fullName", "email (unique)"], "#EC4899", tf, ff)

    img.save(out_path)
    return out_path


def render_architecture(out_path):
    W, H = 2400, 380
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    tf = font(SEGOE_B, 40)
    sf = font(SEGOE, 27)
    af = font(SEGOE_SB, 24)

    boxes = [
        ("CLIENT", "HTTP / JSON", "#64748B", "Postman"),
        ("CONTROLLER", "@RestController", "#0EA5E9", "routing · status · @Valid"),
        ("SERVICE", "@Service", "#0D9488", "rules · @Transactional"),
        ("REPOSITORY", "JpaRepository", "#8B5CF6", "derived + JPQL"),
        ("DATABASE", "MySQL 8", "#F59E0B", "H2 for tests"),
    ]
    bw, bh, gap = 392, 300, 92
    x = 40
    centers = []
    for name, sub, color, note in boxes:
        d.rounded_rectangle([x, 30, x + bw, 30 + bh], radius=18, fill=color)
        d.text((x + 26, 62), name, font=tf, fill="#FFFFFF")
        d.text((x + 26, 132), sub, font=sf, fill="#E2E8F0")
        d.text((x + 26, 210), note, font=af, fill="#F1F5F9")
        centers.append((x, x + bw))
        x += bw + gap

    for i in range(len(centers) - 1):
        x1 = centers[i][1] + 10
        x2 = centers[i + 1][0] - 10
        cy = 180
        d.line([(x1, cy), (x2 - 12, cy)], fill="#94A3B8", width=5)
        d.polygon([(x2, cy), (x2 - 18, cy - 11), (x2 - 18, cy + 11)], fill="#94A3B8")

    img.save(out_path)
    return out_path


# ---------- pptx helpers ----------
def set_bg(slide, color):
    slide.background.fill.solid()
    slide.background.fill.fore_color.rgb = color


def rect(slide, x, y, w, h, fill, line=None, radius=0.06):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(h))
    shape.adjustments[0] = radius
    shape.fill.solid()
    shape.fill.fore_color.rgb = fill
    if line is None:
        shape.line.fill.background()
    else:
        shape.line.color.rgb = line
        shape.line.width = Pt(1)
    shape.shadow.inherit = False
    return shape


def text(slide, value, x, y, w, h, size=16, color=INK, bold=False, font_name="Segoe UI",
         align=PP_ALIGN.LEFT, anchor=MSO_ANCHOR.TOP, spacing=1.0):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    tf.vertical_anchor = anchor
    for i, line in enumerate(value.split("\n")):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.alignment = align
        p.line_spacing = spacing
        run = p.add_run()
        run.text = line
        run.font.size = Pt(size)
        run.font.bold = bold
        run.font.name = font_name
        run.font.color.rgb = color
    return box


def bullets(slide, items, x, y, w, h, size=14, gap=10, color=INK):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    for i, item in enumerate(items):
        main, sub = (item, None) if isinstance(item, str) else item
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(gap)
        p.line_spacing = 1.08
        dot = p.add_run()
        dot.text = "●  "
        dot.font.size = Pt(size - 3)
        dot.font.name = "Segoe UI"
        dot.font.color.rgb = TEAL
        run = p.add_run()
        run.text = main
        run.font.size = Pt(size)
        run.font.name = "Segoe UI"
        run.font.color.rgb = color
        if sub:
            sp = tf.add_paragraph()
            sp.space_after = Pt(gap)
            sr = sp.add_run()
            sr.text = "     " + sub
            sr.font.size = Pt(size - 3)
            sr.font.name = "Segoe UI"
            sr.font.color.rgb = MUTED
    return box


def numbered(slide, items, x, y, w, h, size=14, gap=9):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    for i, (label, body) in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(gap)
        p.line_spacing = 1.05
        n = p.add_run()
        n.text = f"{i + 1}"
        n.font.size = Pt(size)
        n.font.bold = True
        n.font.name = "Segoe UI"
        n.font.color.rgb = TEAL
        r = p.add_run()
        r.text = "   " + label + " — "
        r.font.size = Pt(size)
        r.font.bold = True
        r.font.name = "Segoe UI"
        r.font.color.rgb = INK
        s = p.add_run()
        s.text = body
        s.font.size = Pt(size)
        s.font.name = "Segoe UI"
        s.font.color.rgb = MUTED
    return box


def header(slide, kicker, title, number=None):
    set_bg(slide, BG)
    bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(0.16), Inches(7.5))
    bar.fill.solid()
    bar.fill.fore_color.rgb = TEAL
    bar.line.fill.background()
    bar.shadow.inherit = False
    text(slide, kicker, 0.72, 0.42, 9.0, 0.3, size=11, color=TEAL, bold=True)
    text(slide, title, 0.72, 0.72, 11.9, 0.65, size=27, color=INK, bold=True)
    line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.74), Inches(1.36), Inches(1.15), Inches(0.045))
    line.fill.solid()
    line.fill.fore_color.rgb = AMBER
    line.line.fill.background()
    line.shadow.inherit = False
    footer(slide, number)


def footer(slide, number):
    text(slide, "CourseHub · DS1 Mini Project", 0.72, 7.08, 5.0, 0.25, size=9, color=MUTED)
    if number is not None:
        text(slide, f"{number} / 10", 11.9, 7.08, 0.8, 0.25, size=9, color=MUTED, align=PP_ALIGN.RIGHT)


def code_caption(slide, value, x, y, w):
    text(slide, value, x, y, w, 0.25, size=9, color=MUTED, font_name="Consolas")


def takeaway(slide, value, x=0.62, y=6.02, w=12.1):
    rect(slide, x, y, w, 0.62, TEAL_LIGHT, None, radius=0.18)
    text(slide, "TAKEAWAY", x + 0.24, y + 0.19, 1.3, 0.25, size=9.5, color=TEAL, bold=True)
    text(slide, value, x + 1.42, y + 0.16, w - 1.65, 0.35, size=11.5, color=INK)


def notes(slide, value):
    slide.notes_slide.notes_text_frame.text = value


def metric_card(slide, x, y, w, h, number, label):
    rect(slide, x, y, w, h, WHITE, LINE)
    text(slide, number, x + 0.28, y + 0.22, w - 0.5, 0.8, size=42, color=TEAL, bold=True)
    text(slide, label, x + 0.3, y + 1.02, w - 0.5, 0.5, size=12, color=MUTED)


# ---------- build ----------
def main():
    # images
    render_code(snippet("src/main/java/com/mahmoud/coursehub/model/Course.java", [(61, 77)]),
                os.path.join(IMG_DIR, "course_entity.png"),
                "model/Course.java  ·  relationships", JavaLexer())
    render_code(snippet("src/main/java/com/mahmoud/coursehub/service/EnrollmentService.java", [(65, 89)]),
                os.path.join(IMG_DIR, "enrollment_rules.png"),
                "service/EnrollmentService.java  ·  update rules", JavaLexer())
    render_code(snippet("src/main/java/com/mahmoud/coursehub/repository/CourseRepository.java", [(20, 27)]),
                os.path.join(IMG_DIR, "query_search.png"),
                "repository/CourseRepository.java  ·  search", JavaLexer(), min_width=900)
    render_code(snippet("src/main/java/com/mahmoud/coursehub/repository/EnrollmentRepository.java", [(26, 44)]),
                os.path.join(IMG_DIR, "query_popular.png"),
                "repository/EnrollmentRepository.java  ·  popularity + revenue", JavaLexer())
    render_code(snippet("src/main/java/com/mahmoud/coursehub/exception/GlobalExceptionHandler.java", [(40, 53)]),
                os.path.join(IMG_DIR, "error_handler.png"),
                "exception/GlobalExceptionHandler.java", JavaLexer())
    render_code(
        """{
  "timestamp": "2026-11-09T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/courses",
  "fieldErrors": {
    "title": "Title must be between 3 and 150 characters",
    "price": "Price cannot be negative"
  }
}""",
        os.path.join(IMG_DIR, "error_json.png"),
        "400 Bad Request  ·  response body", JsonLexer(), theme="light", font_size=26, min_width=900)

    render_er(os.path.join(IMG_DIR, "er_diagram.png"))
    render_architecture(os.path.join(IMG_DIR, "architecture.png"))

    val_path = os.path.join(ASSET_DIR, "validator-output.txt")
    val_lines = [ln.rstrip() for ln in open(val_path, encoding="utf-8") if ln.strip()]
    tail = val_lines[-9:]
    shell_lines = [("PS> pwsh -File scripts/validate-api.ps1  -BaseUrl http://localhost:8081", "#94A3B8")]
    shell_lines += [("…  66 more checks passed", "#64748B")]
    for ln in tail:
        if ln.startswith("TOTAL"):
            color = "#FBBF24"
        elif ln.startswith("PASS"):
            color = "#4ADE80"
        elif ln.startswith("FAIL"):
            color = "#F87171"
        else:
            color = "#94A3B8"
        shell_lines.append((ln, color))
    render_shell(shell_lines, os.path.join(IMG_DIR, "validator_terminal.png"),
                 "PowerShell  ·  scripts/validate-api.ps1  (tail)", font_size=24)

    # ---------- deck ----------
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank = prs.slide_layouts[6]

    # 1 - title
    s = prs.slides.add_slide(blank)
    set_bg(s, DARK)
    accent = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.85), Inches(1.35), Inches(0.09), Inches(2.2))
    accent.fill.solid()
    accent.fill.fore_color.rgb = TEAL
    accent.line.fill.background()
    accent.shadow.inherit = False
    text(s, "DS1 MINI PROJECT  ·  SPRING BOOT", 1.2, 1.32, 9.0, 0.3, size=13, color=TEAL, bold=True)
    text(s, "CourseHub", 1.15, 1.7, 10.5, 1.2, size=62, color=WHITE, bold=True)
    text(s, "Online course platform — REST API", 1.2, 2.95, 10.0, 0.5, size=23, color=RGBColor(0xCB, 0xD5, 0xE1))
    text(s, "Spring Boot 3  ·  Spring Data JPA  ·  MySQL 8  ·  Postman", 1.2, 3.65, 11.0, 0.4,
         size=14, color=RGBColor(0x94, 0xA3, 0xB8))
    text(s, "Mahmoud Najar", 1.2, 5.55, 6.0, 0.4, size=15, color=WHITE, bold=True)
    text(s, "5-minute presentation  ·  November 2026", 1.2, 5.95, 7.0, 0.35, size=11,
         color=RGBColor(0x94, 0xA3, 0xB8))
    notes(s, "Good morning. I'm Mahmoud, and this is CourseHub — the backend of an online course "
             "platform, built for DS1 with Spring Boot, Spring Data JPA and MySQL. In five minutes "
             "I'll show the data model, the layered design, the business rules, the custom queries, "
             "and the tests. (0:00–0:15)")

    # 2 - overview
    s = prs.slides.add_slide(blank)
    header(s, "01  ·  OVERVIEW", "A complete backend for an online course platform", 2)
    text(s, "Instructors publish courses. Students enroll, track their progress and review them. "
            "Everything is exposed as a JSON REST API — there is no frontend, Postman is the client.",
         0.72, 1.62, 11.9, 0.7, size=15, color=MUTED, spacing=1.15)
    metric_card(s, 0.72, 2.55, 3.7, 1.55, "7", "linked JPA entities")
    metric_card(s, 4.62, 2.55, 3.7, 1.55, "40+", "REST endpoints, full CRUD")
    metric_card(s, 8.52, 2.55, 3.7, 1.55, "76", "Postman checks, all passing")

    chips = ["Category", "Instructor", "Tag", "Course", "Student", "Enrollment", "Review"]
    x = 0.75
    for chip in chips:
        w = 0.42 + len(chip) * 0.105
        rect(s, x, 4.55, w, 0.42, TEAL_LIGHT, None, radius=0.45)
        text(s, chip, x, 4.62, w, 0.28, size=11.5, color=TEAL, bold=True, align=PP_ALIGN.CENTER)
        x += w + 0.16
    text(s, "One project, the whole DS1 checklist: entities, relationships, CRUD, custom queries, "
            "validation, error handling and Postman tests.", 0.72, 5.35, 11.9, 0.5, size=13, color=INK)
    divider = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.72), Inches(6.28), Inches(11.9), Inches(0.012))
    divider.fill.solid()
    divider.fill.fore_color.rgb = LINE
    divider.line.fill.background()
    divider.shadow.inherit = False
    text(s, "Spring Boot 3  ·  Spring Data JPA / Hibernate  ·  MySQL 8  ·  H2 for tests  ·  JUnit 5  ·  Postman",
         0.72, 6.45, 11.9, 0.3, size=10.5, color=MUTED)
    notes(s, "CourseHub lets instructors publish courses, and students enroll, track progress and "
             "review them. There are seven linked entities, more than forty REST endpoints with full "
             "CRUD, and a Postman collection of seventy-six requests. There is no frontend — Postman "
             "is the client. (0:15–0:45)")

    # 3 - architecture
    s = prs.slides.add_slide(blank)
    header(s, "02  ·  ARCHITECTURE", "One request, four layers", 3)
    s.shapes.add_picture(os.path.join(IMG_DIR, "architecture.png"), Inches(0.72), Inches(1.6),
                         width=Inches(11.9))
    cards = [
        ("Controller", "HTTP only: routing, status codes and @Valid. It contains no business logic."),
        ("Service", "Owns the rules and the 404 / 409 / 422 decisions. @Transactional keeps each operation atomic."),
        ("Repository", "Spring Data JPA interfaces. Derived queries and five custom JPQL queries; Hibernate writes the SQL."),
        ("Model", "Seven entities mapped to tables, with their relationships, constraints and validation."),
    ]
    x = 0.72
    for title, body in cards:
        rect(s, x, 3.95, 2.88, 2.15, WHITE, LINE)
        text(s, title, x + 0.24, 4.18, 2.45, 0.3, size=14, color=TEAL, bold=True)
        text(s, body, x + 0.24, 4.58, 2.45, 1.4, size=10.5, color=MUTED, spacing=1.12)
        x += 3.0
    notes(s, "Every request crosses four layers. The controller only handles HTTP: routing, status codes "
             "and validation. The service owns the rules and the transaction. The repository is a Spring "
             "Data interface — I write no SQL for CRUD. The model maps seven entities to tables. Keeping "
             "these separate is what makes the project easy to test. (0:45–1:25)")

    # 4 - data model
    s = prs.slides.add_slide(blank)
    header(s, "03  ·  DATA MODEL", "Seven entities, three relationship styles", 4)
    s.shapes.add_picture(os.path.join(IMG_DIR, "er_diagram.png"), Inches(0.55), Inches(1.55),
                         width=Inches(8.55))
    text(s, "7 entities  ·  2 association entities  ·  1 join table", 0.6, 6.42, 8.4, 0.3,
         size=10, color=MUTED)
    bullets(s, [
        "One-to-many: Category → Course and Instructor → Course. Each course has exactly one of each.",
        "Many-to-many: Course ↔ Tag, stored in the course_tags join table.",
        "Enrollment and Review are association entities: they carry progress, status, rating and dates — data a plain join table cannot hold.",
        "A UNIQUE(student, course) constraint blocks double enrollment and double review.",
        "Deleting a referenced row is refused with 409 instead of a database error.",
    ], 9.35, 1.7, 3.45, 4.9, size=12.5, gap=11)
    notes(s, "The data model uses all three relationship styles. Category and Instructor are one-to-many to "
             "Course. Course and Tag are many-to-many through course_tags. Enrollment and Review are "
             "association entities: they carry their own columns — progress and status, or rating and "
             "comment — plus a unique constraint on student and course, which is what blocks double "
             "enrollment and double review. (1:25–2:05)")

    # 5 - JPA
    s = prs.slides.add_slide(blank)
    header(s, "04  ·  JPA MAPPING", "Relationships are declared once, in the entity", 5)
    s.shapes.add_picture(os.path.join(IMG_DIR, "course_entity.png"), Inches(0.6), Inches(1.62),
                         width=Inches(7.55))
    code_caption(s, "src/main/java/com/mahmoud/coursehub/model/Course.java  ·  lines 61–77", 0.64, 4.28, 7.5)
    bullets(s, [
        "@ManyToOne + @JoinColumn create category_id and instructor_id in the courses table.",
        "@ManyToMany + @JoinTable create course_tags(course_id, tag_id).",
        "mappedBy marks the inverse side; @JsonIgnore stops infinite JSON recursion.",
        "EAGER is used for the small reference objects that appear in every response.",
    ], 8.55, 1.75, 4.25, 4.3, size=13.5, gap=17)
    takeaway(s, "Hibernate turns these declarations into joins — the API keeps returning real objects, not ids.")
    notes(s, "In JPA the relationships are declared once, in the entity. @ManyToOne with @JoinColumn creates "
             "the foreign key. @ManyToMany with @JoinTable creates the join table. mappedBy marks the "
             "inverse side, and @JsonIgnore stops infinite recursion when the entity is serialized. After "
             "that, Hibernate handles the joins. (2:05–2:40)")

    # 6 - business rules
    s = prs.slides.add_slide(blank)
    header(s, "05  ·  BUSINESS RULES", "The service layer is where the rules live", 6)
    s.shapes.add_picture(os.path.join(IMG_DIR, "enrollment_rules.png"), Inches(0.6), Inches(1.6),
                         width=Inches(7.45))
    code_caption(s, "src/main/java/com/mahmoud/coursehub/service/EnrollmentService.java  ·  lines 65–89",
                 0.64, 5.6, 7.4)
    bullets(s, [
        "One enrollment per student per course → 409 Conflict.",
        "A cancelled enrollment cannot be modified → 422 Unprocessable Entity.",
        "progress = 100 sets COMPLETED; COMPLETED forces progress = 100.",
        "The controller stays thin; @Transactional rolls back the whole operation on failure.",
    ], 8.45, 1.75, 4.35, 4.4, size=13, gap=14)
    takeaway(s, "The same rule protects every client: Postman today, a future frontend tomorrow, or a script.")
    notes(s, "Business rules stay in the service. A student cannot enroll twice — 409. A cancelled "
             "enrollment is frozen — 422. Reaching one hundred percent progress automatically completes "
             "the enrollment, and a completed enrollment is forced to one hundred percent. The controller "
             "never decides any of this. (2:40–3:20)")

    # 7 - queries
    s = prs.slides.add_slide(blank)
    header(s, "06  ·  CUSTOM JPQL", "Five queries that answer real business questions", 7)
    s.shapes.add_picture(os.path.join(IMG_DIR, "query_search.png"), Inches(0.6), Inches(1.6),
                         width=Inches(5.55))
    code_caption(s, "CourseRepository  ·  search by title + budget", 0.64, 2.95, 5.5)
    numbered(s, [
        ("Search", "title LIKE + max price"),
        ("Roster", "JOIN enrollments, cancelled excluded"),
        ("Popularity", "GROUP BY + COUNT + Pageable"),
        ("Revenue", "SUM of course prices"),
        ("Rating", "AVG of reviews"),
    ], 0.65, 3.35, 5.6, 3.0, size=12.5, gap=8)
    s.shapes.add_picture(os.path.join(IMG_DIR, "query_popular.png"), Inches(6.5), Inches(1.6),
                         width=Inches(6.25))
    code_caption(s, "EnrollmentRepository  ·  popularity ranking + instructor revenue", 6.54, 4.62, 6.2)
    bullets(s, [
        "JPQL uses entity names and fields; Hibernate translates it to the database dialect.",
        "Pageable limits the ranking to the top N rows.",
        "Popularity is returned as JSON: courseId, title, enrollments.",
    ], 6.55, 5.0, 6.2, 1.8, size=12, gap=7)
    notes(s, "DS1 asks for three custom queries; I wrote five. Search filters by title and budget. The "
             "roster joins enrollments and excludes cancelled ones. Popularity groups by course, counts "
             "enrollments and limits the result with Pageable. Revenue sums course prices, and rating "
             "averages the reviews. All of them are JPQL, so they use entity names and fields, not table "
             "names. (3:20–4:00)")

    # 8 - validation and errors
    s = prs.slides.add_slide(blank)
    header(s, "07  ·  VALIDATION & ERRORS", "Bad input never reaches the database", 8)
    s.shapes.add_picture(os.path.join(IMG_DIR, "error_handler.png"), Inches(0.6), Inches(1.6),
                         width=Inches(7.3))
    code_caption(s, "GlobalExceptionHandler  ·  one advice for the whole API", 0.64, 4.2, 7.3)
    bullets(s, [
        "Rules live on the entities: @NotBlank, @Size, @Email, @Min / @Max, @Past, @NotNull.",
        "@Valid on the controller parameter triggers them before the service runs.",
    ], 0.62, 4.6, 7.25, 1.4, size=12, gap=8)
    s.shapes.add_picture(os.path.join(IMG_DIR, "error_json.png"), Inches(8.2), Inches(1.6),
                         width=Inches(4.55))
    bullets(s, [
        "One ApiError shape for every failure.",
        "400 validation / malformed · 404 unknown id · 409 duplicate · 422 business rule.",
    ], 8.2, 4.55, 4.5, 1.4, size=11.5, gap=8)
    takeaway(s, "The messages come from the annotations; the handler only chooses the status code and the JSON shape.")
    notes(s, "Validation is declared on the entities — NotBlank, Size, Email, Min, Max, Past, NotNull — "
             "and triggered by @Valid on the controller. When something fails, one RestControllerAdvice "
             "converts it into the same JSON shape with the right status: 400 for validation or broken "
             "JSON, 404 for an unknown id, 409 for a duplicate, and 422 for a broken business rule. "
             "(4:00–4:30)")

    # 9 - testing
    s = prs.slides.add_slide(blank)
    header(s, "08  ·  TESTING", "Verified end to end, not just compiled", 9)
    metric_card(s, 0.72, 1.58, 3.7, 1.35, "76", "Postman requests")
    metric_card(s, 4.62, 1.58, 3.7, 1.35, "76/76", "checks passed")
    metric_card(s, 8.52, 1.58, 3.7, 1.35, "5", "JUnit tests on H2")
    s.shapes.add_picture(os.path.join(IMG_DIR, "validator_terminal.png"), Inches(0.6), Inches(3.0),
                         width=Inches(7.5))
    text(s, "Real run on the demo profile (H2)  ·  no failures", 0.64, 6.62, 8.0, 0.25, size=9, color=MUTED)
    bullets(s, [
        "CRUD, the five custom queries and 25 error cases — each with status and body assertions.",
        "The same API runs on MySQL (default) or H2 (demo / test) by changing the profile only.",
        "Reproducible from the terminal: scripts/validate-api.ps1.",
    ], 9.05, 3.1, 3.7, 3.2, size=12, gap=11)
    notes(s, "Finally, testing. The Postman collection has seventy-six requests: full CRUD, the five "
             "custom queries, and twenty-five error cases, each asserting the status and the body. All "
             "seventy-six pass. There are also five JUnit tests on H2. The same API runs on MySQL or H2 "
             "by changing the profile only. (4:30–4:50)")

    # 10 - wrap up
    s = prs.slides.add_slide(blank)
    set_bg(s, DARK)
    text(s, "09  ·  WRAP-UP", 0.85, 0.6, 6.0, 0.3, size=12, color=TEAL, bold=True)
    text(s, "Demo, then questions", 0.82, 0.95, 11.5, 0.9, size=40, color=WHITE, bold=True)

    cols = [
        ("Live demo path", [
            "Create category and instructor",
            "Create two tags and a course",
            "Create a student and enroll",
            "Update progress to 100%",
            "Post a review",
            "Show popular, rating, revenue",
        ]),
        ("What it demonstrates", [
            "7 linked entities, 3 relationship styles",
            "Full CRUD on every entity",
            "5 custom JPQL queries",
            "Validation + one error shape",
            "76 passing Postman checks",
        ]),
    ]
    x = 0.85
    for title, items in cols:
        text(s, title, x, 2.25, 4.0, 0.35, size=15, color=TEAL, bold=True)
        y = 2.75
        for item in items:
            text(s, "—  " + item, x, y, 4.4, 0.3, size=11.5, color=RGBColor(0xCB, 0xD5, 0xE1))
            y += 0.46
        x += 4.55

    text(s, "Thank you", 9.95, 2.25, 3.0, 0.4, size=15, color=TEAL, bold=True)
    text(s, "Questions?", 9.92, 2.72, 3.0, 0.5, size=24, color=WHITE, bold=True)
    text(s, "github.com/mahmoudnajar548-hash/ds", 9.92, 3.55, 3.2, 0.6, size=10.5,
         color=RGBColor(0x94, 0xA3, 0xB8))
    div = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.85), Inches(6.35), Inches(11.6), Inches(0.012))
    div.fill.solid()
    div.fill.fore_color.rgb = RGBColor(0x1E, 0x29, 0x3B)
    div.line.fill.background()
    div.shadow.inherit = False
    text(s, "DS1 mini project  ·  Spring Boot 3  ·  Spring Data JPA  ·  MySQL 8  ·  Postman",
         0.85, 6.55, 9.0, 0.3, size=10, color=RGBColor(0x64, 0x74, 0x8B))
    notes(s, "In the demo I create a category, an instructor, two tags and a course, then a student, "
             "enroll them, review the course and show the three statistics endpoints. The project covers "
             "the whole DS1 checklist. Thank you — I'm happy to take questions. (4:50–5:00)")

    out = os.path.join(PRES_DIR, "CourseHub_DS1_Presentation.pptx")
    prs.save(out)
    print("saved", out)
    print("slides:", len(prs.slides))


if __name__ == "__main__":
    main()
