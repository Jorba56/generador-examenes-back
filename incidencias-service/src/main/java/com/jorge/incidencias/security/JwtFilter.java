package com.jorge.incidencias.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    // TIENE QUE SER EXACTAMENTE LA MISMA QUE EN USUARIOS-SERVICE
    private static final String SECRET_KEY=System.getenv("SECRET_KEY") != null ? System.getenv("SECRET_KEY") : "clave_secreta_por_defecto_para_tests";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
                DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

                String username = decodedJWT.getSubject();
                // Asumimos que tu token guarda el rol en un claim llamado "rol" o "role"
                // Si en usuarios no lo guardas, luego te digo cómo añadirlo.
                List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

                if (roles == null || roles.isEmpty()) {
                    roles = Collections.singletonList("USER");
                }

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                for (String rol : roles) {
                    authorities.add(new SimpleGrantedAuthority(rol.toUpperCase()));
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                java.util.logging.Logger.getLogger(JwtFilter.class.getName())
                        .warning("Token inválido en incidencias: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}