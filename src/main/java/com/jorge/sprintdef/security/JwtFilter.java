package com.jorge.sprintdef.security;

import com.jorge.sprintdef.User;
import com.jorge.sprintdef.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtUtil.validarTokenYObtenerEmail(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // buscamos al usuario en la BD para saber sus roles reales
                    User usuario = userRepository.findUserByEmailUsuario(email);

                    if (usuario != null) {
                        // convertimos roles de la bd al formato spring security
                        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                                .map(rol -> new SimpleGrantedAuthority(rol.getName().toUpperCase()))
                                .collect(Collectors.toList());

                        // metemos los roles en el contexto de seguridad
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // para token inválido o expirado
                e.printStackTrace();
            }
        }
            filterChain.doFilter(request, response);
        }
    }
