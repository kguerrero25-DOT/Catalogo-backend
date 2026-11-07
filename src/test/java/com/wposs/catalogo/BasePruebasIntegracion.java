package com.wposs.catalogo;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

abstract class BasePruebasIntegracion {
    static final String JWT_SECRET = secretoAleatorio();

    @DynamicPropertySource
    static void registrarSecretoDePrueba(DynamicPropertyRegistry propiedades) {
        propiedades.add("app.jwt.secreto", () -> JWT_SECRET);
    }

    private static String secretoAleatorio() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
