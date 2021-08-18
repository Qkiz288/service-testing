package com.herokuapp.restbooker;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

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

    @Test
    public void headersAndCookiesTest() {
        String testHeaderName = "TEST_HEADER";
        String testHeaderValue = "TEST_HEADER_VALUE";
        Header testHeader = new Header(testHeaderName, testHeaderValue);

        String testCookieName = "TEST_COOKIE";
        String testCookieValue = "TEST_COOKIE_VALUE";
        Cookie testCookie = new Cookie.Builder(testCookieName, testCookieValue).build();

        Response response = RestAssured.given(getRequestSpec())
                .header(testHeader)
                .cookie(testCookie)
                .log()
                .all()
                .get("/ping");

        assertThat("Response should contains 2xx Status Code", response.getStatusCode(),
                CoreMatchers.equalTo(201));
    }

}
