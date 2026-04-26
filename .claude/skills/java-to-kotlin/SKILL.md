---
name: java-to-kotlin
description: >
  Migrates Java source files to idiomatic Kotlin in a Spring Boot (Maven) project.
  Handles records→data classes, constants classes→objects, interfaces, utility
  classes→top-level functions, null safety, checked exceptions, and Spring
  annotation compatibility. Use this skill whenever the user asks to convert,
  migrate, translate, or rewrite Java files to Kotlin — even for single files,
  even if they just say "convert this to Kotlin" or "make this Kotlin". Also
  triggers when asked to "kotlinize" code or "use Kotlin instead of Java".
---

## Goal

Convert one or more Java source files to idiomatic, compilable Kotlin while
preserving behaviour and Spring Boot compatibility. Prefer Kotlin idioms over
mechanical transliteration.

## Workflow

### Step 1 – Assess the build (Maven)

Read `pom.xml`. If `kotlin-stdlib` and `kotlin-maven-plugin` are not yet
present, add them before converting any source files. See
[references/maven-setup.md](references/maven-setup.md) for the exact snippet.

Skip this step if Kotlin is already configured.

### Step 2 – Plan the conversion scope

List the Java files to convert. Group them by conversion difficulty:

| Tier | Pattern | Example |
|------|---------|---------|
| Easy | Constants-only class, simple record/DTO | `ApiEndPoint.java`, `UserAssem.java` |
| Medium | Record with factory/helper methods, interface | `DeviceInfo.java`, `GeminiService.java` |
| Hard | Large stateful class, Spring controller/service | `HomeController.java`, `KakakuClient.java` |

Convert Easy → Medium → Hard. Present the plan and wait for approval if the
user is interactive; otherwise proceed.

### Step 3 – Convert each file

Apply the patterns in [references/conversion-patterns.md](references/conversion-patterns.md).

Key rules:
- Move the `.java` file to a `.kt` file in the **same package directory**.
- Do not change package declarations.
- Do not leave dead `.java` files after conversion — delete them.
- Run `./mvnw compile` (or `./mvnw test`) after each tier to catch regressions early.

### Step 4 – Validate

After all conversions:

```bash
./mvnw test
```

If tests fail, diagnose and fix before reporting done.

### Step 5 – Report

Summarise what was converted, any idioms applied (e.g., extension functions,
`object` vs `companion object`), and any decisions that required judgment.
