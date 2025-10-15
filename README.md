# Swagger Core Polymorphism Caching Bug Reproduction

[![CI](https://github.com/benjaminknauer/swagger-core/actions/workflows/ci.yml/badge.svg)](https://github.com/benjaminknauer/swagger-core/actions/workflows/ci.yml)

This project demonstrates a polymorphism-related caching bug in **Swagger Core v2.2.39** and later.

The underlying issue is tracked in [swagger-api/swagger-core#5003](https://github.com/swagger-api/swagger-core/issues/5003), and this repository serves as a reproducer.

## The Bug: Incomplete `allOf` Schema

When a polymorphic class is resolved in different contexts (e.g., first as a property, then as a subtype), the `ModelConverterContext` can return a partially populated schema. The resulting OpenAPI definition is missing the child-specific properties. The regression first appears after swagger-core commit [dd5ce5445d74e2173446f5b6238f7f3d42bf58e6](https://github.com/swagger-api/swagger-core/commit/dd5ce5445d74e2173446f5b6238f7f3d42bf58e6).

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
