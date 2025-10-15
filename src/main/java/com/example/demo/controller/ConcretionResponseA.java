package com.example.demo.controller;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("concretiona")
public class ConcretionResponseA extends BaseResponse {

	private String concretionAField;

	public String getConcretionAField() {
		return concretionAField;
	}

	public void setConcretionAField(String concretionAField) {
		this.concretionAField = concretionAField;
	}
}
