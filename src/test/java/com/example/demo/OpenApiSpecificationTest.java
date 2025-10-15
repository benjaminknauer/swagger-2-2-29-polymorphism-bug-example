package com.example.demo;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiSpecificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldMatchCompleteOpenApiStructure() throws Exception {
        String expectedJson = """
            {
              "openapi": "3.0.1",
              "info": {
                "title": "OpenAPI definition",
                "version": "v0"
              },
              "servers": [
                {
                  "url": "http://localhost",
                  "description": "Generated server url"
                }
              ],
              "paths": {
                "/concretion": {
                  "get": {
                    "tags": [
                      "inheritance-controller"
                    ],
                    "operationId": "getBaseConcretionClassWithMeta",
                    "responses": {
                      "200": {
                        "description": "OK",
                        "content": {
                          "application/json": {
                            "schema": {
                              "oneOf": [
                                {
                                  "$ref": "#/components/schemas/BaseResponse"
                                },
                                {
                                  "$ref": "#/components/schemas/ConcretionResponseA"
                                },
                                {
                                  "$ref": "#/components/schemas/ConcretionResponseB"
                                }
                              ]
                            }
                          }
                        }
                      }
                    }
                  }
                },
                "/base": {
                  "get": {
                    "tags": [
                      "inheritance-controller"
                    ],
                    "operationId": "getBaseClassWithMeta",
                    "responses": {
                      "200": {
                        "description": "OK",
                        "content": {
                          "application/json": {
                            "schema": {
                              "oneOf": [
                                {
                                  "$ref": "#/components/schemas/BaseResponse"
                                },
                                {
                                  "$ref": "#/components/schemas/ConcretionResponseA"
                                },
                                {
                                  "$ref": "#/components/schemas/ConcretionResponseB"
                                }
                              ]
                            }
                          }
                        }
                      }
                    }
                  }
                }
              },
              "components": {
                "schemas": {
                  "BaseResponse": {
                    "required": [
                      "type"
                    ],
                    "type": "object",
                    "properties": {
                      "baseField": {
                        "type": "string"
                      },
                      "type": {
                        "type": "string"
                      }
                    },
                    "discriminator": {
                      "propertyName": "type"
                    }
                  },
                  "ConcretionResponseA": {
                    "type": "object",
                    "allOf": [
                      {
                        "$ref": "#/components/schemas/BaseResponse"
                      },
                      {
                        "type": "object",
                        "properties": {
                          "concretionAField": {
                            "type": "string"
                          }
                        }
                      }
                    ]
                  },
                  "ConcretionResponseB": {
                    "type": "object",
                    "allOf": [
                      {
                        "$ref": "#/components/schemas/BaseResponse"
                      },
                      {
                        "type": "object",
                        "properties": {
                          "concretionBField": {
                            "type": "string"
                          }
                        }
                      }
                    ]
                  }
                }
              }
           }
           """;

        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String actualJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }
}
