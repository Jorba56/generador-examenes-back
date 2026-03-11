package com.jorge.sprintdef.services;

import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.entity.Rol;
import com.jorge.sprintdef.entity.User;
import com.jorge.sprintdef.exceptions.BadRequestException;
import com.jorge.sprintdef.exceptions.DuplicateException;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {
    List<UsersAllDTO> listarUsuarios();

    UserIdDTo buscarPorId(Long id);

    UsersAllDTO addUsuario(UserAddDTO usuario) throws DuplicateException;

    String actualizarUsuario(Long id, User usuario, Authentication authentication) throws BadRequestException;

    String desactivarUsuario(Long id);

    List<Rol> rolesUser(Long idUser);

    String addRolUser(Long idUser, RolPostUser idRol) throws DuplicateException;

    String deleteRolUser(Long idUser, Long idRol);
}
