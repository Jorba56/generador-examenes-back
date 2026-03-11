package com.jorge.sprintdef.services;

import com.jorge.sprintdef.dto.LoginDTO;
import com.jorge.sprintdef.exceptions.BadRequestException;
import com.jorge.sprintdef.exceptions.ConflictException;

import java.util.Map;

public interface AuthService {
    Map<String, String> login(LoginDTO loginDto) throws BadRequestException, ConflictException;
}
