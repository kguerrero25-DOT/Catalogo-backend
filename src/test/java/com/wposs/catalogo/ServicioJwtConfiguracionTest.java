package com.wposs.catalogo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wposs.catalogo.servicio.ServicioJwt;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

class ServicioJwtConfiguracionTest {
    @Test
    void noCreaElServicioSiFaltaLaVariableDeEntorno() {
        new ApplicationContextRunner()
                .withUserConfiguration(ConfiguracionJwt.class)
                .withInitializer(contexto -> {
                    ConfigurableEnvironment entorno = (ConfigurableEnvironment) contexto.getEnvironment();
                    entorno.getPropertySources().remove("systemEnvironment");
                    entorno.getPropertySources().remove("systemProperties");
                    entorno.getPropertySources().addFirst(new MapPropertySource("jwt-properties", Map.of(
                            "app.jwt.secreto", "${JWT_SECRETO}",
                            "app.jwt.duracion-ms", "3600000")));
                })
                .run(contexto -> {
                    Throwable fallo = contexto.getStartupFailure();
                    assertNotNull(fallo);
                    assertTrue(NestedExceptionUtils.getMostSpecificCause(fallo)
                            .getMessage().contains("JWT_SECRETO"));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ServicioJwt.class)
    static class ConfiguracionJwt { }
}
