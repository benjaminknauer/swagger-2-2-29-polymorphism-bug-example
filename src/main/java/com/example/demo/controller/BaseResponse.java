package com.example.demo.controller;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "myType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ConcretionResponseA.class, name = "concretiona"),
		@JsonSubTypes.Type(value = ConcretionResponseB.class, name = "concretionb")
})
public class BaseResponse {

	private String baseField;

	public String getBaseField() {
		return baseField;
	}

	public void setBaseField(String baseField) {
		this.baseField = baseField;
	}
}
