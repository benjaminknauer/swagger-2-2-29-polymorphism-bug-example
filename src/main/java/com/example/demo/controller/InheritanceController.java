package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
