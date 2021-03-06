package com.herokuapp.restbooker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.herokuapp.model.*;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class BookingTest extends BaseTest {

    private static final String ALL_BOOKINGS = "/booking";
    private static final String BOOKING_BY_ID = "/booking/{id}";
    private static final String AUTH = "/auth";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password123";

    private static final String NEW_USER_FIRSTNAME = "Kamil";
    private static final String NEW_USER_LASTNAME = "K";
    private static final LocalDate NEW_USER_CHECKIN = LocalDate.now();
    private static final LocalDate NEW_USER_CHECKOUT = LocalDate.now().plusWeeks(1);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ObjectMapper xmlMapper = new XmlMapper().registerModule(new JavaTimeModule());
    private String token;
    private Long createdBookingId;

    @BeforeClass
    public void getToken() throws JsonProcessingException {
        Credentials credentials = Credentials.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        String credentialsBody = objectMapper.writeValueAsString(credentials);
        Response response = getRequestSpec()
                .body(credentialsBody)
                .post(AUTH);
        token = response.jsonPath().get("token");
    }

    @Test
    public void getBookingIdsWithoutFilter() {
        int expectedStatusCode = 200;

        Response response = getRequestSpec().get(ALL_BOOKINGS);

        assertThat(String.format("HTTP Status Code should be %s", expectedStatusCode),
                response.getStatusCode(), equalTo(expectedStatusCode));

        List<Integer> bookingIds = response.jsonPath().getList("bookingId");
        assertThat("bookingId list should not be empty", bookingIds, is(not(empty())));
    }

    @Test
    public void getBookingById() {
        int id = 1;
        Response response = getRequestSpec().pathParam("id", id).get(BOOKING_BY_ID);

        String fname = response.jsonPath().get("firstname");
        String lname = response.jsonPath().get("lastname");

        SoftAssert softAssert = new SoftAssert();

        softAssert.assertNotNull(fname, "firstname value shouldn't be null");
        softAssert.assertNotNull(lname, "lastname value shouldn't be null");
        softAssert.assertAll();
    }

    @Test
    public void getBookingByIdXmlResponse() throws JsonProcessingException {
        int id = 1;
        Header xmlHeader = new Header("Accept", "application/xml");
        Response response = getRequestSpec()
                .header(xmlHeader)
                .pathParam("id", id)
                .get(BOOKING_BY_ID);

        Booking booking = xmlMapper.readValue(response.getBody().asString(), Booking.class);
        assertThat("First name shouldn't be null", response.xmlPath().getString("booking.firstname"),
                allOf(notNullValue(), not(equalTo(""))));
        assertThat("Last name shouldn't be null", booking.getLastname(),
                allOf(notNullValue(), not(equalTo(""))));
    }

    @Test
    public void createNewBooking() {
        Booking booking = createBasicBooking();

        Response response = getRequestSpec()
                .body(booking)
                .post(ALL_BOOKINGS);
        assertThat("Response status code should be 200", response.getStatusCode(), equalTo(200));

        BookingResponse bookingResponse = response.as(BookingResponse.class);
        assertThat("New booking ID shouldn't be null", bookingResponse.getBookingid(), notNullValue());

        createdBookingId = bookingResponse.getBookingid();
        softAssertAllBookingResponseFields(bookingResponse.getBooking(), booking);
    }

    @Test(dependsOnMethods = "createNewBooking")
    public void getBookingIdsFilterByName() throws JsonProcessingException {
        Response response = getRequestSpec().queryParam("firstname", NEW_USER_FIRSTNAME)
                .queryParam("lastname", NEW_USER_LASTNAME).get(ALL_BOOKINGS);
        List<BookingId> bookingIds = Arrays.asList(objectMapper.readValue(response.getBody().asString(),
                BookingId[].class));

        assertThat("Booking IDs list should not be empty", bookingIds, is(Matchers.not(empty())));
    }

    @Test(dependsOnMethods = "getBookingIdsFilterByName")
    public void deleteBooking() throws JsonProcessingException {
        getRequestSpec()
                .pathParam("id", createdBookingId)
                .auth()
                .preemptive()
                .basic(USERNAME, PASSWORD)
                .delete(BOOKING_BY_ID);

        Response allBookingsResponse = getRequestSpec().get(ALL_BOOKINGS);
        List<BookingId> bookings = Arrays.asList(objectMapper.readValue(
                allBookingsResponse.getBody().asString(), BookingId[].class));

        Optional<BookingId> deletedBookingOptional = bookings.stream()
                .filter(bookingId -> bookingId.getBookingid().equals(createdBookingId))
                .findFirst();

        assertThat(String.format("Booking with ID = %s should be deleted", createdBookingId),
                deletedBookingOptional.isEmpty());
    }

    @Test
    public void updateBooking() throws JsonProcessingException {
        Integer id = 1;
        Booking booking = createBasicBooking();
        String requestBody = objectMapper.writeValueAsString(booking);

        Response response = getRequestSpec().body(requestBody)
                .cookie("token", token)
                .pathParam("id", id)
                .put(BOOKING_BY_ID);
        assertThat("Response status code should be 200", response.getStatusCode(), equalTo(200));

        Booking bookingResponse = objectMapper.readValue(response.getBody().asString(), Booking.class);
        softAssertAllBookingResponseFields(bookingResponse, booking);
    }

    @Test
    public void patchBooking() throws JsonProcessingException {
        Integer id = 1;
        String newName = "Peter";

        Response response = getRequestSpec().
                pathParam("id", id)
                .get(BOOKING_BY_ID);
        Booking booking = objectMapper.readValue(response.getBody().asString(), Booking.class);

        Response secondResponse = getRequestSpec()
                .cookie("token", token)
                .pathParam("id", id)
                .body("{\"firstname\":\"Peter\"}")
                .patch(BOOKING_BY_ID);
        Booking patchedBooking = objectMapper.readValue(secondResponse.getBody().asString(), Booking.class);

        booking.setFirstname(newName);
        softAssertAllBookingResponseFields(patchedBooking, booking);
    }

    private Booking createBasicBooking() {
        Integer totalPrice = 45;
        Boolean depositPaid = true;
        String additionalNeeds = "Nothing";

        return Booking.builder()
                .firstname(NEW_USER_FIRSTNAME)
                .lastname(NEW_USER_LASTNAME)
                .totalprice(totalPrice)
                .depositpaid(depositPaid)
                .bookingdates(BookingDates.builder()
                        .checkin(NEW_USER_CHECKIN)
                        .checkout(NEW_USER_CHECKOUT)
                        .build())
                .additionalneeds(additionalNeeds)
                .build();
    }

    private void softAssertAllBookingResponseFields(Booking booking, Booking expectedBooking) {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(booking);
        softAssert.assertEquals(booking.getFirstname(), expectedBooking.getFirstname());
        softAssert.assertEquals(booking.getLastname(), expectedBooking.getLastname());
        softAssert.assertEquals(booking.getTotalprice(), expectedBooking.getTotalprice());
        softAssert.assertEquals(booking.getDepositpaid(), expectedBooking.getDepositpaid());
        softAssert.assertEquals(booking.getBookingdates().getCheckin(), expectedBooking.getBookingdates().getCheckin());
        softAssert.assertEquals(booking.getBookingdates().getCheckout(), expectedBooking.getBookingdates().getCheckout());
        softAssert.assertEquals(booking.getAdditionalneeds(), expectedBooking.getAdditionalneeds());
        softAssert.assertAll();
    }

}
