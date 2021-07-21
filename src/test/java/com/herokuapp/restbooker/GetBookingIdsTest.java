package com.herokuapp.restbooker;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class GetBookingIdsTest {

    private static final String ALL_BOOKINGS = "/booking";
    private static final String BOOKING_BY_ID = "booking/{id}";

    RequestSpecification requestSpec;

    @BeforeMethod
    public void setUp() {
        requestSpec = given().baseUri("https://restful-booker.herokuapp.com");
    }

    @Test
    public void getBookingIdsWithoutFilter() {
        int expectedStatusCode = 200;

        Response response = requestSpec.get(ALL_BOOKINGS);

        assertThat(String.format("HTTP Status Code should be %s", expectedStatusCode),
                response.getStatusCode(), equalTo(expectedStatusCode));

        List<Integer> bookingIds = response.jsonPath().getList("bookingId");
        assertThat("bookingId list should not be empty", bookingIds, is(not(empty())));
    }

    @Test
    public void getBookingById() {
        int id = 1;
        Response response = requestSpec.pathParam("id", id).get(BOOKING_BY_ID);

        String fname = response.jsonPath().get("firstname");
        String lname = response.jsonPath().get("lastname");

        SoftAssert softAssert = new SoftAssert();

        softAssert.assertNotNull(fname, "firstname value shouldn't be null");
        softAssert.assertNotNull(lname, "lastname value shouldn't be null");
        softAssert.assertAll();
    }
}
