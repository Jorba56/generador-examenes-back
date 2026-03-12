package com.jorge.sprintdef.services;

import com.jorge.sprintdef.dto.LoginDTO;
import com.jorge.sprintdef.exceptions.BadRequestException;
import com.jorge.sprintdef.exceptions.ConflictException;

import java.util.Map;

/**
 * Interfaz que define las operaciones de autenticación de usuarios.
 */
public interface AuthService {
    /**
     * Valida las credenciales del usuario y genera un token de acceso.
     *
     * @param loginDto Objeto con las credenciales (email y contraseña) del usuario.
     * @return Un mapa que contiene el token JWT generado bajo la clave "token".
     * @throws BadRequestException Si las credenciales son incorrectas.
     * @throws ConflictException Si la cuenta del usuario está desactivada.
     */
    Map<String, String> login(LoginDTO loginDto) throws BadRequestException, ConflictException;
}
