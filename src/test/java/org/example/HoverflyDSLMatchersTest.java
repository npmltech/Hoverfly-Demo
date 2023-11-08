package org.example;

import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import model.SimpleBooking;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.xml;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.created;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.OK;

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
                .get(startsWith("/api/bookings/")) // Matches request path that starts with /api/bookings/
                .queryParam("page", matches("1*"))
                .queryParam("description", any()) // Matches page query with any value
                .willReturn(success(json(HoverflyDSLMatchersTest.BOOKING)))

                .get("/api/bookings/online")
                .anyQueryParams() // Match any query params
                .willReturn(success(json(BOOKING)))

                .put("/api/bookings/1")
                .body(equalsToJson(json(HoverflyDSLMatchersTest.BOOKING))) // Matches body which equals to a JSON Object
                .willReturn(success())

                .put("/api/bookings/1")
                .body(matchesPartialJson(json(HoverflyDSLMatchersTest.BOOKING))) // Matches body which is a superset of a JSON Object (Partial JSON matching)
                .willReturn(success())

                .post("/api/bookings")
                .body(matchesJsonPath("$.flightId")) // Matches body with a JSON path expression
                .willReturn(created("https://localhost/api/bookings/1"))

                .put("/api/bookings/1")
                .body(equalsToXml(xml(HoverflyDSLMatchersTest.BOOKING))) // Matches body which equals to XML object
                .willReturn(success())

                // XmlPath Matcher
                .post("/api/bookings")
                .body(matchesXPath("/flightId")) // Matches body with a xpath expression
                .willReturn(created("https://localhost/api/bookings/1"))
            //
        )
    ).printSimulationData();

    @Test
    public void shouldQueryBookingWithQueryParams() {
        // When
        this.uri = UriComponentsBuilder
            .fromHttpUrl("https://www.xpto-test.com")
            .path("/api/bookings/1")
            .queryParam("page", 12)
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

        Throwable throwable = catchThrowable(() -> restTemplate.getForEntity(this.uri, SimpleBooking.class));

        // Then
        assertThat(throwable).isInstanceOf(HttpServerErrorException.class);

        HttpServerErrorException exception = (HttpServerErrorException) throwable;

        assertThat(exception.getStatusCode()).isEqualTo(BAD_GATEWAY);
        assertThat(exception.getResponseBodyAsString()).containsIgnoringCase("Hoverfly error");
    }

    @Test
    public void shouldQueryBookingWithAnyQueryParams() {
        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("https://www.xpto-test.com")
            .path("/api/bookings/online")
            .queryParam("wherever", "wherever")
            .build()
            .toUri();

        ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(BOOKING);
    }
}