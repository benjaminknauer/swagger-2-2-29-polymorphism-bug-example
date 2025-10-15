package com.example.demo.controller;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("concretionb")
public class ConcretionResponseB extends BaseResponse {

	private String concretionBField;

	public String getConcretionBField() {
		return concretionBField;
	}

	public void setConcretionBField(String concretionBField) {
		this.concretionBField = concretionBField;
	}
}
