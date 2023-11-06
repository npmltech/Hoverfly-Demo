package org.example;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static java.lang.System.out;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoverflyDSLTest {

    private RestTemplate restTemplate;

    public HoverflyDSLTest() {
        restTemplate = new RestTemplate();
    }

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
        dsl(
            service("www.my-test.com")
                .get("/api/bookings/1")
                .willReturn(success("{\"bookingId\":\"1\"}", "application/json")
            )
        )
    );

    @Test
    public void shouldBeAbleToGetABookingUsingHoverfly() {
        // When
        final ResponseEntity<String> getBookingResponse = this.restTemplate
            .getForEntity("http://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
        assertThatJson(getBookingResponse.getBody()).isEqualTo("{\"bookingId\":\"1\"}");
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverflyUsingRestAssured() {
        // Given
        RestAssured.baseURI = "http://www.my-test.com";
        RequestSpecification request =
            RestAssured
                .given()
                .config(RestAssured
                    .config()
                    .sslConfig(new SSLConfig()
                        .allowAllHostnames()
                        .relaxedHTTPSValidation()
                    )
                    .encoderConfig(EncoderConfig
                        .encoderConfig()
                        .encodeContentTypeAs(
                            "application json",
                            ContentType.JSON
                        )
                    )
                )
                .accept("application/json")
                .contentType("application/json; charset=UTF-8");

        // When
        Response response =
            request
                .when()
                .get("/api/bookings/1");

        // Then
        int statusCode = response.then().extract().statusCode();
        String result  = response.then().extract().response().asPrettyString();

        out.printf("Response result: %s%n", result);
        out.printf("Status code: %s%n", response.getStatusCode());

        assertThat(statusCode).isEqualTo(200);
        assertThatJson(result).isEqualTo("{\"bookingId\":\"1\"}");
    }
}
