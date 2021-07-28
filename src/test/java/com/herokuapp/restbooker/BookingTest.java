package com.herokuapp.restbooker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.herokuapp.model.Booking;
import com.herokuapp.model.BookingDates;
import com.herokuapp.model.BookingResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class BookingTest {

    private static final String ALL_BOOKINGS = "/booking";
    private static final String BOOKING_BY_ID = "/booking/{id}";

    RequestSpecification requestSpec;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeMethod
    public void setUp() {
        requestSpec = given()
                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
                .baseUri("https://restful-booker.herokuapp.com");
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

    @Test
    public void createNewBooking() throws JsonProcessingException {
        String fname = "Kamil";
        String lname = "K";
        Integer totalPrice = 45;
        Boolean depositPaid = true;
        LocalDate checkin = LocalDate.now();
        LocalDate checkout = LocalDate.now().plusWeeks(1);
        String additionalNeeds = "Nothing";

        Booking booking = Booking.builder()
                .firstname(fname)
                .lastname(lname)
                .totalprice(totalPrice)
                .depositpaid(depositPaid)
                .bookingdates(BookingDates.builder()
                        .checkin(checkin)
                        .checkout(checkout)
                        .build())
                .additionalneeds(additionalNeeds)
                .build();
        String requestBody = objectMapper.writeValueAsString(booking);

        Response response = requestSpec
                .body(requestBody)
                .post(ALL_BOOKINGS);
        assertThat("Response status code should be 200", response.getStatusCode(), equalTo(200));
        BookingResponse bookingResponse = objectMapper.readValue(response.getBody().asString(), BookingResponse.class);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(bookingResponse);
        softAssert.assertNotNull(bookingResponse.getBookingid());
        Booking bResponse = bookingResponse.getBooking();
        softAssert.assertEquals(bResponse.getFirstname(), fname);
        softAssert.assertEquals(bResponse.getLastname(), lname);
        softAssert.assertEquals(bResponse.getTotalprice(), totalPrice);
        softAssert.assertEquals(bResponse.getDepositpaid(), depositPaid);
        softAssert.assertEquals(bResponse.getBookingdates().getCheckin(), checkin);
        softAssert.assertEquals(bResponse.getBookingdates().getCheckout(), checkout);
        softAssert.assertEquals(bResponse.getAdditionalneeds(), additionalNeeds);
        softAssert.assertAll();
    }

}
