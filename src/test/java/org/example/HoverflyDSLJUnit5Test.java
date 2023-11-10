package org.example;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@HoverflyCore(
    config = @HoverflyConfig(adminPort = 9000, proxyPort = 9001),
    mode = HoverflyMode.CAPTURE
)
@ExtendWith(HoverflyExtension.class)
public class HoverflyDSLJUnit5Test {

    @Test
    void shouldTestSomethingWith(Hoverfly hoverfly) {
        assertThat(hoverfly.isHealthy()).isTrue();
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
        assertThat(hoverfly.getHoverflyConfig().getAdminPort()).isEqualTo(9000);
        assertThat(hoverfly.getHoverflyConfig().getProxyPort()).isEqualTo(9001);
    }
}
