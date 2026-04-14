# Project Development Guidelines

To ensure the maintainability and type safety of the WeCom SCRM platform, all developers must adhere to the following standards.

## 1. Database Query Specifications

Database query results must use explicitly defined **Entity** or **DTO (Data Transfer Object)** classes. 

- **Avoid Generic Types**: Never use `Object[]`, `Map<String, Object>`, or raw `List` for query results. These are difficult to maintain and prone to runtime errors.
- **Constructor Projection**: For repository methods return complex results, use the JPQL `SELECT new com.wecom.scrm.dto.TargetDTO(...)` syntax.
- **Interface Projections**: For simple read-only data, Spring Data JPA interface projections are also acceptable, provided they have clear getter methods.

> [!IMPORTANT]
> All new DTOs should be placed in the `com.wecom.scrm.dto` package and use descriptive names (e.g., `AddWayStatDTO` instead of `Stat1`).

---

## 2. API Interface Specifications

All parameters exchanged with the frontend or external systems must be explicitly modeled as **Request/Response Objects** (VO/DTO).

- **No Generic Maps**: Do not use `Map<String, Object>` as controller method parameters or return types. This obscures the API contract and breaks auto-generated documentation.
- **Strict Parsing**: Avoid manually parsing JSON strings into `Map` or `JSONObject` for logic processing. Use Jackson/Spring's automatic binding to specific POJOs.
- **Contract Clarity**: Use descriptive field names and valid validation annotations (e.g., `@NotBlank`, `@NotNull`) to define a clear and self-documenting API.

> [!WARNING]
> Hardcoding keys for `Map` access (e.g., `map.get("userId")`) is strictly prohibited as it is extremely fragile and lacks compile-time safety.

---

## 3. Java Import Standards

Maintain a clean and readable codebase by managing imports correctly.

- **No Full-Path References**: Never use fully qualified class names (e.g., `com.wecom.scrm.dto.CustomerTargetDTO`) inside the method body or as return types in the class signature.
- **Explicit Imports**: Always use the `import` statement at the top of the file to include required classes.
- **Clean Namespace**: If there are naming conflicts, resolve them through alias or careful class naming rather than using inline full-paths.

> [!TIP]
> Keeping the code free of full-path clutter makes it easier to read and allows IDEs to optimize imports efficiently.

---

## 3. Maintenance

These guidelines are living documents. When refactoring existing code, always strive to bring legacy sections into compliance with these standards.
