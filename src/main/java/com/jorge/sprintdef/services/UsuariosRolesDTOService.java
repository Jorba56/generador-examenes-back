package com.jorge.sprintdef.services;

import com.jorge.sprintdef.*;
import com.jorge.sprintdef.dto.*;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de gestionar operaciones de transformación de datos (Data Transfer Objects)
 * relacionadas específicamente con la relación entre los usuarios y sus roles asignados.
 */
@Service
public class UsuariosRolesDTOService {
    private final UserRepository userRep;

    public UsuariosRolesDTOService( UserRepository userRep){
        this.userRep=userRep;
    }

    /**
     * Recupera todos los usuarios activos del sistema y transforma la información en una lista de DTOs.
     * Extrae de forma limpia el ID de cada usuario junto con una lista que contiene únicamente los IDs de sus roles,
     * ocultando así información sensible o redundante para respuestas más ligeras de la API.
     *
     * @return Una lista de objetos {@link UsuarioRolesDTO} correspondientes a los usuarios activos.
     */
    public List<UsuarioRolesDTO> listaUsuariosRoles() {
        List<UsuarioRolesDTO> usersRoles = new ArrayList<>();
        List<User> usuarios = userRep.findUsersByActivoIs(true);
        for (User usuario : usuarios) {
            UsuarioRolesDTO dto = new UsuarioRolesDTO();
            List<Long> ids = new ArrayList<>();
            dto.setIdUser(usuario.getIdUser());
            List<Rol> roles = usuario.getRoles();
            for (Rol role : roles) {
                ids.add(role.getIdRol());
            }
            dto.setIdRoles(ids);
            usersRoles.add(dto);
        }
        return usersRoles;
    }


}

