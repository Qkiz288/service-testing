package com.herokuapp.restbooker;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class HealthCheck {

    private static final String HEALTH_CHECK = "https://restful-booker.herokuapp.com/ping";

    @Test
    public void healthCheckTest() {
        given()
                .when()
                .get(HEALTH_CHECK)
                .then()
                .assertThat()
                .statusCode(201);
    }

}
