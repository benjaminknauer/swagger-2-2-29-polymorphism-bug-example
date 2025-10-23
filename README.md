# Swagger Core Schema Caching Bug Reproduction

[![CI](https://github.com/benjaminknauer/swagger-core/actions/workflows/ci.yml/badge.svg)](https://github.com/benjaminknauer/swagger-core/actions/workflows/ci.yml)

This project demonstrates a schema caching bug in **Swagger Core v2.2.39** and later that causes incomplete component definitions when the same Java type is resolved in multiple contexts. The sample application uses a polymorphic response hierarchy to trigger the issue, but the underlying problem is the cache reusing the first, context-specific schema for every subsequent resolution.

The underlying issue is tracked in [swagger-api/swagger-core#5003](https://github.com/swagger-api/swagger-core/issues/5003), and this repository serves as a reproducer.

## The Bug: Incomplete `allOf` Schema

When the same model class is resolved first as a property (for example inside a wrapper object) and later as a standalone schema, the `ModelConverterContext` can return a partially populated result. In the generated OpenAPI document the second `allOf` element—containing the subtype-specific properties—is missing. The regression first appears after swagger-core commit [dd5ce5445d74e2173446f5b6238f7f3d42bf58e6](https://github.com/swagger-api/swagger-core/commit/dd5ce5445d74e2173446f5b6238f7f3d42bf58e6).

In this project the controller is extremely small, yet it still hits the bug because `ConcretionResponseA` is resolved twice: once while processing the `/base` endpoint (as part of the `BaseResponse` hierarchy) and a second time as the direct response of `/concretion`. The first resolution caches an incomplete schema, the second resolution reuses it.

```java
// src/main/java/com/example/demo/controller/InheritanceController.java
@RestController
public class InheritanceController {

    @GetMapping(path = "/base", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse getBaseClassWithMeta() {
        return new BaseResponse();
    }

    @GetMapping(path = "/concretion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConcretionResponseA getConcretionClassWithMeta() {
        return new ConcretionResponseA();
    }
}

// src/main/java/com/example/demo/controller/BaseResponse.java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "myType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConcretionResponseA.class, name = "concretiona"),
        @JsonSubTypes.Type(value = ConcretionResponseB.class, name = "concretionb")
})
public class BaseResponse {
    private String baseField;
    // getters/setters omitted
}

// src/main/java/com/example/demo/controller/ConcretionResponseA.java
@JsonTypeName("concretiona")
public class ConcretionResponseA extends BaseResponse {
    private String concretionAField;
    // getters/setters omitted
}

// src/main/java/com/example/demo/controller/ConcretionResponseB.java
@JsonTypeName("concretionb")
public class ConcretionResponseB extends BaseResponse {
    private String concretionBField;
    // getters/setters omitted
}
```

### Expected Schema

```json
"ConcretionResponseA": {
    "type": "object",
    "allOf": [
        { "$ref": "#/components/schemas/BaseResponse" },
        {
            "type": "object",
            "properties": {
                "concretionAField": { "type": "string" }
            }
        }
    ]
}
```

### Actual Schema (Buggy)

```json
"ConcretionResponseA": {
    "allOf": [
        { "$ref": "#/components/schemas/BaseResponse" }
    ]
}
```

## Reproducing the Bug

Run the test suite to generate and compare the OpenAPI schema:
```bash
mvn clean test
```

The `BuggySchemaIntegrationTest` asserts that the schema should match the expected `allOf` structure. The failure output shows that the serialized response from `/api-docs` lacks the subtype properties, confirming the bug.

### Possible cause

The regression stems from commit `dd5ce5445d74e2173446f5b6238f7f3d42bf58e6`, which normalizes context annotations inside `AnnotatedType.equals()/hashCode()` but still ignores flags such as `schemaProperty` and `propertyName`. As a result, property and subtype resolutions now collapse into the same cache entry and the subtype-specific fragment is skipped.
