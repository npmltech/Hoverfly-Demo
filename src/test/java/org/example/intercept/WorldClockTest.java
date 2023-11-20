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
        "{\"$id\":\"1\", " +
        "\"currentDateTime\":\"2023-11-10T20:59+01:00\", " +
        "\"utcOffset\":\"01:00:00\", " +
        "\"isDayLightSavingsTime\":"+ false +", " +
        "\"dayOfTheWeek\":\"Friday\", " +
        "\"timeZoneName\":\"Central Europe Standard Time\", " +
        "\"currentFileTime\":\"133441235489726188\", " +
        "\"ordinalDate\":\"2023-314\", " +
        "\"serviceResponse\":"+ null +", " +
    "}";

    @Test
    public void shouldGetTimeFromExternalService(Hoverfly hoverfly) throws IllegalAccessException {
        // Given
        hoverfly.simulate(
            SimulationSource.dsl(
                HoverflyDsl
                    .service("http://worldclockapi.com")
                    .get("/api/json/cet/now")
                    .queryParam("callback", "mycallback")
                    .willReturn(success(OUTPUT, "application/json")
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
