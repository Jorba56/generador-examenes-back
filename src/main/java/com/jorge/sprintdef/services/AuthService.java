package com.jorge.sprintdef.services;

import com.jorge.sprintdef.User;
import com.jorge.sprintdef.UserRepository;
import com.jorge.sprintdef.dto.LoginDTO;
import com.jorge.sprintdef.exceptions.BadRequestException;
import com.jorge.sprintdef.exceptions.ConflictException;
import com.jorge.sprintdef.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRep;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRep, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRep = userRep;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, String> login(LoginDTO loginDto) {
        User usuario = userRep.findUserByEmailUsuario(loginDto.getEmailUsuario());

        // matches() compara la contraseña plana que viene del JSON con el hash guardado en la BD
        if (usuario == null || !passwordEncoder.matches(loginDto.getContrasenhaUsuario(), usuario.getContrasenhaUsuario())) {
            throw new BadRequestException("Credenciales de acceso incorrectas.");
        }

        if (!usuario.getActivo()) {
            throw new ConflictException("La cuenta de usuario se encuentra desactivada.");
        }

        // Generamos el token y lo devolvemos
        String token = jwtUtil.generarToken(usuario.getEmailUsuario());
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", token);

        return respuesta;
    }
}