package org.example;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.value;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.OK;


public class HoverflyMiddlewareTest {

    private final RestTemplate restTemplate;

    private URI uri;

    private static final String TEST_RSRC = "/src/test/resources/";

    public HoverflyMiddlewareTest() {
        restTemplate = new RestTemplate();
    }

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
            classpath("middleware-1/simulation.json"),
            localConfigs()
                .localMiddleware(
                    System.getProperty("user.dir")
                        .concat(
                            "%srun-jar.sh".formatted(HoverflyMiddlewareTest.TEST_RSRC)
                        ),
                    //
                    "middleware-1/empty.json"
                    //
                )
                .disableTlsVerification()
            //
        //
    ).printSimulationData();

    public void emulateMiddleware() {
        try (Hoverfly hoverfly = new Hoverfly(
            localConfigs()
                .localMiddleware(
                    System.getProperty("user.dir")
                        .concat(
                            "%srun-jar.sh".formatted(HoverflyMiddlewareTest.TEST_RSRC)
                        ),
                    //
                    "middleware-1/empty.json"
                    //
                ),
            SIMULATE
        )
        ) {
            hoverfly.start();
            hoverfly.simulate(
                classpath("middleware-1/simulation.json")
            );
            hoverfly.getHoverflyConfig();
        }
    }

    @Test
    public void shouldAccessTheLocalMiddleware() {
        // When
        this.uri = UriComponentsBuilder
            .fromHttpUrl("http://www.my-test.com")
            .path("/api/version")
            .build()
            .toUri();
        //

        ResponseEntity<String> response = this.restTemplate.getForEntity(this.uri, String.class);

        String responseString = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThatJson(responseString)
            .inPath("errors[*].source.pointer")
            .isArray()
            .containsAnyOf(value("/data/attributes/firstName"));
        assertThatJson(responseString)
            .node("errors")
            .isArray()
            .containsExactly("{\"detail\":\"Firstnamemustcontainatleasttwocharacters.\",\"source\":{\"pointer\":\"/data/attributes/firstName\"},\"status\":\"422\",\"title\":\"InvalidAttribute\"}");
        //
    }
}
