package com.herokuapp.restbooker;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class GetBookingIdsTest {

    RequestSpecification requestSpec;

    @BeforeMethod
    public void setUp() {
        requestSpec = given().baseUri("https://restful-booker.herokuapp.com");
    }

    @Test
    public void getBookingIdsWithoutFilter() {
        int expectedStatusCode = 200;

        Response response = requestSpec.when().get("/booking");

        assertThat(String.format("HTTP Status Code should be %s", expectedStatusCode),
                response.getStatusCode(), equalTo(expectedStatusCode));

        List<Integer> bookingIds = response.jsonPath().getList("bookingId");
        assertThat("bookingId list should not be empty", bookingIds, is(not(empty())));
    }
}
