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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.*;
import static java.lang.System.out;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoverflyDSLTest {

    private final RestTemplate restTemplate;
    private static RequestSpecification request;
    private static Response response;

    public HoverflyDSLTest() {
        restTemplate = new RestTemplate();
    }

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
        dsl(
            service("www.my-test.com")
                // GET
                .get("/api/bookings/1")
                .willReturn(success("{\"bookingId\":\"1\"}", "application/json"))
                // POST
                .post("/api/bookings")
                .body("{\"flightId\": \"1\"}")
                // PUT
                .willReturn(created("https://localhost/api/bookings/1"))
                .put("/api/bookings/1")
                .body("{\"flightId\": \"1\", \"class\": \"PREMIUM\"}")
                .willReturn(success())
                // DELETE
                .delete("/api/bookings/1")
                .willReturn(noContent())
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
        HoverflyDSLTest.response =
            HoverflyDSLTest.request
                .when()
                .get("/api/bookings/1");

        // Then
        int statusCode = HoverflyDSLTest.response.then().extract().statusCode();
        String result = HoverflyDSLTest.response.then().extract().response().asPrettyString();

        out.printf("Status code: %s%n", response.getStatusCode());
        out.printf("Response result: %s%n", result);

        assertThat(statusCode).isEqualTo(200);
        assertThatJson(result).isEqualTo("{\"bookingId\":\"1\"}");
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverflyUsingRestAssuredPostMethod() {
        // When
        HoverflyDSLTest.response =
            HoverflyDSLTest.request
                .body("{\"flightId\": \"1\"}")
                .when()
                .post("/api/bookings");

        // Then
        int statusCode = HoverflyDSLTest.response.then().extract().statusCode();
        String header = HoverflyDSLTest.response.then().extract().headers().get("Location").getValue();

        out.printf("Status code: %s%n", response.getStatusCode());
        out.printf("Header content: %s%n", header);

        assertThat(statusCode).isEqualTo(201);
        assertThat(header).isEqualTo("https://localhost/api/bookings/1");
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverflyUsingRestAssuredPutMethod() {
        // When
        HoverflyDSLTest.response =
            HoverflyDSLTest.request
                .body("{\"flightId\": \"1\", \"class\": \"PREMIUM\"}")
                .when()
                .put("/api/bookings/1");

        // Then
        int statusCode = HoverflyDSLTest.response.then().extract().statusCode();

        out.printf("Status code: %s%n", HoverflyDSLTest.response.getStatusCode());

        assertThat(statusCode).isEqualTo(200);
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverflyUsingRestAssuredDeleteMethod() {
        // When
        HoverflyDSLTest.response =
            HoverflyDSLTest.request
                .when()
                .delete("/api/bookings/1");

        // Then
        int statusCode = HoverflyDSLTest.response.then().extract().statusCode();

        out.printf("Status code: %s%n", response.getStatusCode());

        assertThat(statusCode).isEqualTo(204);
    }
}
