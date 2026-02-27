package com.jorge.sprintdef.services;

import com.jorge.sprintdef.*;
import com.jorge.sprintdef.dto.*;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsuariosRolesDTOService {
    private final UserRepository userRep;

    public UsuariosRolesDTOService( UserRepository userRep){
        this.userRep=userRep;
    }

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

