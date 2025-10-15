# Swagger Core Polymorphism Bug Example

![CI](https://github.com/benjaminknauer/swagger-2-2-29-polymorphism-bug-example/actions/workflows/ci.yml/badge.svg)

## The Bug

**Broken since:** Swagger Core 2.2.39+
**Last working version:** 2.2.38

When returning a **generic base type** (e.g., `BaseResponse`), OpenAPI should generate a `oneOf` schema with all possible subtypes. When returning a **concrete implementation** (e.g., `ConcretionResponseA`), OpenAPI should reference only that specific schema without `oneOf`.

### Expected Behavior (≤ 2.2.38)

- Generic return type → `oneOf` with discriminator
- Concrete return type → Direct schema reference (no `oneOf`)

### Actual Behavior (≥ 2.2.39)

Schema generation for polymorphic types is broken. This project pins to **2.2.38** to demonstrate the correct behavior.

## Example

```java
// Generic return type - should generate oneOf
@GetMapping("/base")
public BaseResponse getBaseClassWithMeta() {
    return new BaseResponse();
}

// Concrete return type - should NOT generate oneOf
@GetMapping("/concrete")
public ConcretionResponseA getConcreteResponse() {
    return new ConcretionResponseA();
}
```

**In v2.2.38:** Works correctly
**In v2.2.39+:** Broken schema generation

### Polymorphism Setup

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConcretionResponseA.class, name = "concretiona"),
    @JsonSubTypes.Type(value = ConcretionResponseB.class, name = "concretionb")
})
public class BaseResponse { ... }
```

## Running

```bash
# Run application
mvn spring-boot:run

# Run tests
mvn test

# View OpenAPI docs
open http://localhost:8080/swagger-ui.html
```

## Verification

The test `OpenApiSpecificationTest` validates the expected behavior with v2.2.38:
- Generic return types produce `oneOf` schemas with discriminator
- Correct `allOf` inheritance for concrete implementations
- Proper discriminator property mapping

**Stack:** Spring Boot 3.3.5 • springdoc-openapi 2.6.0 • Swagger Core **2.2.38** • Java 17

---

To reproduce the bug, change `swagger.version` in `pom.xml` to `2.2.39` or higher and run tests.
