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
        for (int i = 0; i < usuarios.size(); i++) {
            UsuarioRolesDTO dto = new UsuarioRolesDTO();
            List<Long> ids = new ArrayList<>();
            dto.setIdUser(usuarios.get(i).getIdUser());
            List<Rol> roles = usuarios.get(i).getRoles();
            for (int j = 0; j < roles.size(); j++) {
                ids.add(roles.get(j).getIdRol());
            }
            dto.setIdRoles(ids);
            usersRoles.add(dto);
        }
        return usersRoles;
    }


}

