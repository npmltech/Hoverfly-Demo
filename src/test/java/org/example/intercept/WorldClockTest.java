package org.example.intercept;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.dsl.HoverflyDsl;
import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalTime;

import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(HoverflyExtension.class)
public class WorldClockTest {

    private static final String OUTPUT =
        "{\"$id\":\"1\"," +
        "\"currentDateTime\":\"2023-11-25T20:59+01:00\"" +
        ",\"utcOffset\":\"01:00:00\"," +
        "\"isDayLightSavingsTime\":" + false + "," +
        "\"dayOfTheWeek\":\"Saturday\"," +
        "\"timeZoneName\":\"CentralEuropeStandardTime\"," +
        "\"currentFileTime\":133454218945357720," +
        "\"ordinalDate\":\"2023-329\"}";

    @Test
    public void shouldGetTimeFromExternalService(Hoverfly hoverfly) throws IllegalAccessException {
        // Given
        hoverfly.simulate(
            SimulationSource.dsl(
                HoverflyDsl
                    .service("http://worldclockapi.com")
                    .get("/api/json/cet/now")
                    .queryParam("callback", "mycallback")
                    .willReturn(success(WorldClockTest.OUTPUT, "application/json")
                )
            )
        );

        // When
        LocalTime localTime = new ConsumingWorldClock().getTime("cet", "?callback=mycallback");

        // Then
        assertThat(localTime.getHour()).isEqualTo(20);
        assertThat(localTime.getMinute()).isEqualTo(59);
    }
}
