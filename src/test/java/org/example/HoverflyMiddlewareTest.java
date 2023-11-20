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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class HoverflyMiddlewareTest {

    private final RestTemplate restTemplate;

    private URI uri;

    private static final String distDir = "/src/test/resources/";

    public HoverflyMiddlewareTest() {
        restTemplate = new RestTemplate();
    }

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
        classpath("middleware-1/simulation.json"),
        localConfigs()
            .localMiddleware(
                HoverflyMiddlewareTest.getMiddlewareJarDirectory(),
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
                    HoverflyMiddlewareTest.getMiddlewareJarDirectory(),
                    "middleware-1/empty.json"
                   //
                ),
                SIMULATE
            )
        )
        {
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
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    private static String getMiddlewareJarDirectory() {
        String distDir = "/src/test/resources/";
        return System.getProperty("user.dir").concat(distDir).concat("run-jar.sh");
    }
}
