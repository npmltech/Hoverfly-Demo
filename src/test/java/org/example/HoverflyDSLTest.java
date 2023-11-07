package org.example;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.created;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static java.lang.System.out;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoverflyDSLTest {

    private final RestTemplate restTemplate;
    private static RequestSpecification request;

    public HoverflyDSLTest() {
        restTemplate = new RestTemplate();
    }

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
        dsl(
            service("www.my-test.com")
                .get("/api/bookings/1")
                .willReturn(success("{\"bookingId\":\"1\"}", "application/json"))
                .post("/api/bookings")
                .body("{\"flightId\": \"1\"}")
                .willReturn(created("http://localhost/api/bookings/1"))
            //
        )
    );

    @Before
    public void setup() {
        // Given
        RestAssured.baseURI = "https://www.my-test.com";
        HoverflyDSLTest.request =
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
                );
            //
        //
    }


    @Test
    public void shouldBeAbleToGetABookingUsingHoverfly() {
        // When
        ResponseEntity<String> getBookingResponse = this.restTemplate
            .getForEntity("https://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
        assertThatJson(getBookingResponse.getBody()).isEqualTo("{\"bookingId\":\"1\"}");
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverflyUsingRestAssuredGetMethod() {
        // When
        Response response =
            HoverflyDSLTest.request
                .when()
                .get("/api/bookings/1");

        // Then
        int statusCode = response.then().extract().statusCode();
        String result = response.then().extract().response().asPrettyString();

        out.printf("Status code: %s%n", response.getStatusCode());
        out.printf("Response result: %s%n", result);

        assertThat(statusCode).isEqualTo(200);
        assertThatJson(result).isEqualTo("{\"bookingId\":\"1\"}");
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverflyUsingRestAssuredPostMethod() {
        // When
        Response response =
            HoverflyDSLTest.request
                .body("{\"flightId\": \"1\"}")
                .when()
                .post("/api/bookings");

        // Then
        int statusCode = response.then().extract().statusCode();
        String header = response.then().extract().headers().get("Location").getValue();

        out.printf("Status code: %s%n", response.getStatusCode());
        out.printf("Header content: %s%n", header);

        assertThat(statusCode).isEqualTo(201);
        assertThat(header).isEqualTo("http://localhost/api/bookings/1");
    }
}
