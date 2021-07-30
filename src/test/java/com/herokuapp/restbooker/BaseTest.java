package com.herokuapp.restbooker;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class BaseTest {
    private static final String BASE_URI = "https://restful-booker.herokuapp.com";
    protected RequestSpecification getRequestSpec() {
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .build()
                .given();
        return given(spec);
    }
}
