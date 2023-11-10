package org.example;

import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import model.SimpleBooking;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.xml;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.*;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoverflyDSLMatchersTest {

    private static final SimpleBooking BOOKING = new SimpleBooking(
        1,
        "London",
        "Hong Kong",
        LocalDate.now()
    );

    private final RestTemplate restTemplate;

    private URI uri;

    public HoverflyDSLMatchersTest() {
        restTemplate = new RestTemplate();
    }

    // Given
    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
        SimulationSource.dsl(
            service(matches("www.*-test.com")) // Matches url with wildcard
                /* --- GET --- */
                .get(startsWith("/api/bookings/")) // Matches request path that starts with /api/bookings/
                .queryParam("page", matches("1*"))
                .queryParam("description", any()) // Matches page query with any value
                .willReturn(success(json(HoverflyDSLMatchersTest.BOOKING)))

                .get("/api/bookings/online")
                .anyQueryParams() // Match any query params
                .willReturn(success(json(BOOKING)))

                 /* --- POST --- */
                .post("/api/bookings")
                .body(matchesJsonPath("$.flightId")) // Matches body with a JSON path expression
                .willReturn(created("https://localhost/api/bookings/1"))

                // XmlPath Matcher
                .post("/api/bookings")
                .body(matchesXPath("/flightId")) // Matches body with a xpath expression
                .willReturn(created("https://localhost/api/bookings/1"))

                /* --- PUT --- */
                .put("/api/bookings/1")
                .body(equalsToJson(json(HoverflyDSLMatchersTest.BOOKING))) // Matches body which equals to a JSON Object
                .willReturn(success())

                .put("/api/bookings/1")
                .body(matchesPartialJson(json(HoverflyDSLMatchersTest.BOOKING))) // Matches body which is a superset of a JSON Object (Partial JSON matching)
                .willReturn(success())

                // Match JSON body
                .put("/api/bookings/1")
                .header("Content-Type", contains("application/json"))
                .body(equalsToJson("{\"flightId\":\"1\",\"class\":\"PREMIUM\"}"))

                .willReturn(success())
                // Match XML body
                .put("/api/bookings/1")
                .body(equalsToXml("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>"))
                .willReturn(success())

                .put("/api/bookings/1")
                .body(equalsToXml(xml(HoverflyDSLMatchersTest.BOOKING))) // Matches body which equals to XML object
                .willReturn(success()),

            // Match any path
            service("www.always-success.com")
                .get(any())
                .willReturn(success()),

            // Match any method
            service("www.booking-is-down.com")
                .anyMethod(startsWith("/api/bookings/"))
                .willReturn(serverError()
                .body("booking is down")),

            // Match any body
            service("www.cloud-service.com")
                .post("/api/v1/containers")
                .body(any())
                .willReturn(created())
            //
        )
    ).printSimulationData();

    @Test
    public void shouldQueryBookingWithQueryParams() {
        // When
        this.uri = UriComponentsBuilder
            .fromHttpUrl("https://www.xpto-test.com")
            .path("/api/bookings/1")
            .queryParam("page", 1)
            .queryParam("description", "XPTO")
            .build()
            .toUri();
            //
        //
        ResponseEntity<SimpleBooking> response = this.restTemplate.getForEntity(this.uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(HoverflyDSLMatchersTest.BOOKING);
    }

    @Test
    public void shouldFailToQueryBookingsIfUnexpectedQueryParamIsPresent() {
        // When
        this.uri = UriComponentsBuilder.fromHttpUrl("https://www.xpto-test.com")
            .path("/api/bookings/1")
            .queryParam("page", 1)
            .build()
            .toUri();

        Throwable throwable = catchThrowable(() -> this.restTemplate.getForEntity(this.uri, SimpleBooking.class));

        // Then
        assertThat(throwable).isInstanceOf(HttpServerErrorException.class);

        HttpServerErrorException exception = (HttpServerErrorException) throwable;

        assertThat(exception.getStatusCode()).isEqualTo(BAD_GATEWAY);
        assertThat(exception.getResponseBodyAsString()).containsIgnoringCase("Hoverfly error");
    }

    @Test
    public void shouldQueryBookingWithAnyQueryParams() {
        // When
        this.uri = UriComponentsBuilder.fromHttpUrl("https://www.xpto-test.com")
            .path("/api/bookings/online")
            .queryParam("wherever", "wherever")
            .build()
            .toUri();

        ResponseEntity<SimpleBooking> response = this.restTemplate.getForEntity(this.uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(BOOKING);
    }

    @Test
    public void shouldReturn200ForAnyGetRequestWhenUsingAnyMatcher() {
        // When
        this.uri = UriComponentsBuilder.fromHttpUrl("http://www.always-success.com")
            .path("/any/api/anything")
            .build()
            .toUri();

        ResponseEntity<String> response = this.restTemplate.getForEntity(this.uri, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldIgnoreHttpSchemeWhenItIsNotSet() {
        // When
        this.uri = UriComponentsBuilder.fromHttpUrl("https://www.always-success.com")
            .path("/any/api/anything")
            .build()
            .toUri();

        ResponseEntity<String> response = this.restTemplate.getForEntity(this.uri, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldReturn500WhenSendingRequestWithAnyMethodToTheBookingIsDownService() {
        // When
        this.uri = UriComponentsBuilder.fromHttpUrl("https://www.booking-is-down.com")
            .path("/api/bookings/12345")
            .build()
            .toUri();

        Throwable throwable = catchThrowable(() -> this.restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class));

        assertThat(throwable).isInstanceOf(HttpServerErrorException.class);

        HttpServerErrorException exception = (HttpServerErrorException) throwable;

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getResponseBodyAsString()).isEqualTo("booking is down");
    }

    @Test
    public void shouldBeAbleToMatchBodyByJsonEquality() throws Exception {
        // Given
        RequestEntity<String> bookFlightRequest = RequestEntity
            .put(
                new URI("https://www.xpto-test.com/api/bookings/1")
            )
            .contentType(APPLICATION_JSON)
            .body("{\"class\": \"PREMIUM\", \"flightId\": \"1\"}");

        // When
        ResponseEntity<String> bookFlightResponse = this.restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldBeAbleToMatchBodyByJsonEqualityWithHttpBodyConverter() throws Exception {
        // Given
        RequestEntity<String> bookFlightRequest = RequestEntity
            .put(
                new URI("https://www.xpto-test.com/api/bookings/1")
            )
            .contentType(APPLICATION_JSON)
            .body(ObjectMapperFactory.getDefaultObjectMapper().writeValueAsString(BOOKING));

        // When
        ResponseEntity<String> bookFlightResponse = this.restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldBeAbleToMatchBodyByXPath() throws Exception {
        // Given
        RequestEntity<String> bookFlightRequest = RequestEntity
            .post(
                new URI("https://www.my-test.com/api/bookings")
            )
            .contentType(APPLICATION_JSON)
            .body("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId>");

        // When
        ResponseEntity<String> bookFlightResponse = this.restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("https://localhost/api/bookings/1"));
    }

    @Test
    public void shouldBeAbleToMatchAnyBody() throws Exception {
        // Given
        RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("https://www.cloud-service.com/api/v1/containers"))
            .contentType(APPLICATION_JSON)
            .body("{ \"Hostname\": \"\", \"Domainname\": \"\", \"User\": \"\"}");

        // When
        ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
    }

    @Test
    public void shouldBeAbleToMatchUsingMultipleBodyMatchers() throws Exception {
        // Given
        RequestEntity<String> bookFlightRequest = RequestEntity
            .put(
                new URI("http://www.my-test.com/api/bookings/1")
            )
            .contentType(APPLICATION_JSON)
            .body("{\"class\": \"BUSINESS\", \"destination\": \"London\"}");

        // When
        ResponseEntity<String> bookFlightResponse = this.restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldFailedIfRequestBodyNotMatchingAllConditions() throws Exception {
        // When
        RequestEntity<String> bookFlightRequest = RequestEntity
            .put(
                new URI("http://www.my-test.com/api/bookings/1")
            )
            .contentType(APPLICATION_JSON)
            .body("{\"class\": \"ECONOMY\", \"destination\": \"London\"}");   // Not matching because body does not contains 'BUSINESS'

        // Then
        assertThatThrownBy(() -> this.restTemplate.exchange(bookFlightRequest, String.class))
            .isInstanceOf(HttpServerErrorException.class)
            .hasMessageContaining("502 Bad Gateway");
        //
    }
}
