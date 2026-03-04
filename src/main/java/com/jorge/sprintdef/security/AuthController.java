package com.jorge.sprintdef.security;


import com.jorge.sprintdef.dto.LoginDTO;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.services.AuthService;
import com.jorge.sprintdef.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService){
        this.userService = userService;
        this.authService=authService;
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }

    @PostMapping("/register")
    public ResponseEntity<UsersAllDTO> registro(@Valid @RequestBody UserAddDTO dto) {
        return ResponseEntity.ok(userService.addUsuario(dto));
    }
}
