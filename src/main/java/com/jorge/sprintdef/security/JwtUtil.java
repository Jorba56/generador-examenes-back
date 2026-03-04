package com.jorge.sprintdef.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = System.getenv("SECRET_KEY"); //recoger variable de entorno
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    public String generarToken(String email) {
        return JWT.create()
                .withSubject(email) // Guardamos el correo en el token
                .withIssuedAt(new Date()) // Fecha de creación
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // Expira en 1 día
                .sign(ALGORITHM);
    }

    public String validarTokenYObtenerEmail(String token) {
        return JWT.require(ALGORITHM)
                .build()
                .verify(token)
                .getSubject();
    }
}