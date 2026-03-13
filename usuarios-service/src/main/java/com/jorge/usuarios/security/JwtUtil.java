package com.jorge.usuarios.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final String SECRET_KEY=System.getenv("SECRET_KEY") != null ? System.getenv("SECRET_KEY") : "clave_secreta_por_defecto_para_tests"; //recoger variable de entorno
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    public String generarToken(String email, List<String> roles) {
        return JWT.create()
                .withSubject(email)// guardamos el correo en el token
                .withClaim("roles", roles)
                .withIssuedAt(new Date()) // fecha de creación
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // expira en 1 día
                .sign(ALGORITHM);
    }

    public String validarTokenYObtenerEmail(String token) {
        return JWT.require(ALGORITHM)
                .build()
                .verify(token)
                .getSubject();
    }
}