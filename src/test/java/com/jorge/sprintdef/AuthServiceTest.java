package com.jorge.sprintdef;


import com.jorge.sprintdef.dto.LoginDTO;
import com.jorge.sprintdef.entity.User;
import com.jorge.sprintdef.exceptions.BadRequestException;

import com.jorge.sprintdef.exceptions.ConflictException;
import com.jorge.sprintdef.mapping.UserMapper;
import com.jorge.sprintdef.repository.UserRepository;
import com.jorge.sprintdef.security.JwtUtil;

import com.jorge.sprintdef.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)

class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMap;

    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @Test
    void login() throws ConflictException, BadRequestException {
        User usuario= new User();
        LoginDTO user= new LoginDTO();
        usuario.setIdUser(1L);
        usuario.setContrasenhaUsuario("1234");
        usuario.setNombreUsuario("luis");
        usuario.setApellidoUsuario("gomez");
        usuario.setEmailUsuario("luis@gmail.com");
        user.setEmailUsuario(usuario.getEmailUsuario());
        user.setContrasenhaUsuario(usuario.getContrasenhaUsuario());

        given(userRepository.findUserByEmailUsuario("luis@gmail.com")).willReturn(usuario);
        given(passwordEncoder.matches(user.getContrasenhaUsuario(), usuario.getContrasenhaUsuario())).willReturn(true);
        given(jwtUtil.generarToken("luis@gmail.com")).willReturn("token123");

        Map<String, String> respuesta= authServiceImpl.login(user);

        assertNotNull(respuesta);
        assertEquals("token123", respuesta.get("token"));
    }

    @Test
    void loginCredencialesIncorrectasEmail(){
        User usuario= new User();
        LoginDTO user= new LoginDTO();
        usuario.setIdUser(1L);
        usuario.setEmailUsuario("luis@gmail.com");
        user.setEmailUsuario("luis123@gmail.com");

        given(userRepository.findUserByEmailUsuario("luis123@gmail.com")).willReturn(null);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> authServiceImpl.login(user)
        );

        assertEquals("Credenciales de acceso incorrectas.", ex.getMessage());
    }

    @Test
    void loginCredencialesIncorrectasPasswd(){
        User usuario= new User();
        LoginDTO user= new LoginDTO();
        usuario.setIdUser(1L);
        usuario.setContrasenhaUsuario("1234");
        usuario.setEmailUsuario("luis@gmail.com");
        user.setEmailUsuario("luis@gmail.com");
        user.setContrasenhaUsuario("6567");

        given(userRepository.findUserByEmailUsuario("luis@gmail.com")).willReturn(usuario);
        given(passwordEncoder.matches(user.getContrasenhaUsuario(), usuario.getContrasenhaUsuario())).willReturn(false);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> authServiceImpl.login(user)
        );

        assertEquals("Credenciales de acceso incorrectas.", ex.getMessage());
    }

    @Test
    void loginInactivo(){
        User usuario= new User();
        LoginDTO user= new LoginDTO();
        usuario.setIdUser(1L);
        usuario.setActivo(false);
        usuario.setContrasenhaUsuario("1234");
        usuario.setEmailUsuario("luis@gmail.com");
        user.setEmailUsuario("luis@gmail.com");
        user.setContrasenhaUsuario("1234");

        given(userRepository.findUserByEmailUsuario("luis@gmail.com")).willReturn(usuario);
        given(passwordEncoder.matches(user.getContrasenhaUsuario(), usuario.getContrasenhaUsuario())).willReturn(true);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> authServiceImpl.login(user)
        );

        assertEquals("La cuenta de usuario se encuentra desactivada.", ex.getMessage());
    }

}
