package com.jorge.sprintdef.security;


import com.jorge.sprintdef.dto.LoginDTO;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.services.AuthService;
import com.jorge.sprintdef.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST público que gestiona la autenticación de la aplicación.
 * Proporciona los endpoints iniciales que no requieren de un Token JWT previo,
 * permitiendo a los usuarios registrarse y obtener credenciales de acceso.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para registro y login de usuarios.")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService){
        this.userService = userService;
        this.authService=authService;
    }
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales del usuario y devuelve un token JWT para acceder a los endpoints protegidos.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un usuario en el sistema, encripta su contraseña y le asigna el rol ALUMNO por defecto.")
    @PostMapping("/register")
    public ResponseEntity<UsersAllDTO> registro(@Valid @RequestBody UserAddDTO dto) {
        return ResponseEntity.ok(userService.addUsuario(dto));
    }
}
