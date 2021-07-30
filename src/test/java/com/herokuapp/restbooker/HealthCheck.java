package com.herokuapp.restbooker;

import org.testng.annotations.Test;

public class HealthCheck extends BaseTest {

    private static final String HEALTH_CHECK = "/ping";

    @Test
    public void healthCheckTest() {
        getRequestSpec()
                .when()
                .get(HEALTH_CHECK)
                .then()
                .assertThat()
                .statusCode(201);
    }

}
