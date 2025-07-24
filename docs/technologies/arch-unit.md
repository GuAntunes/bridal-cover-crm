# Summary: Using ArchUnit in the Bridal Cover CRM Project

## Introduction
[ArchUnit](https://www.archunit.org/) is a Java library for validating architectural rules in projects, ensuring organization and proper layer separation.

## Configuration
The **Bridal Cover CRM** project uses the `@AnalyzeClasses` annotation to analyze only the main project classes:

```java
@AnalyzeClasses(
    packages = "br.com.gustavoantunes.bridalcovercrm",
    importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class
    }
)
```

## Main Rules

### 1. Naming Convention
- **Controllers**: End with `Controller` (`..infrastructure.controller..`).
- **Services**: End with `Service` (`..application.service..`).
- **Use Cases**: Interfaces ending with `UseCase` (`..application.port.in..`).
- **Ports**: Interfaces ending with `Port`, `Repository`, or `Gateway` (`..application.port.out..`).
- **Repositories**: End with `Repository` (`..infrastructure.repository..`).
- **Entities**: No `Entity` suffix (`..domain.model..`).

### 2. Dependencies
- **Domain** should not depend on **Application** or **Infrastructure**.
- **Application** should not depend on **Infrastructure**.

### 3. Spring Restrictions
- `@RestController` and `@Controller` only in `..infrastructure.controller..`.
- `@Service` only in `..application.service..`.
- `@Repository` only in `..infrastructure.repository..`.
- No Spring annotations in `domain`.

### 4. Port Access Rules
- **Input Ports** accessible only by `application.service` and `infrastructure.controller`.
- **Output Ports** accessible only by `application.service` and `infrastructure.repository`.

### 5. Interface Implementation
- **Services** must implement **UseCases** (`..application.port.in..`).
- **Repositories** must implement **Ports** (`..application.port.out..`).

## Conclusion
ArchUnit ensures the architectural integrity of the project, preventing unwanted coupling and maintaining a well-structured organization over time.

[Official ArchUnit Documentation](https://www.archunit.org/)

