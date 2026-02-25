package com.jorge.sprintdef.mapping;

import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.User;
import com.jorge.sprintdef.dto.UsersAllDTO;

public class UserMapper implements UserMapping {

    @Override
    public UsersAllDTO mappingADTO(User usuario){
        UsersAllDTO dto= new UsersAllDTO();
        dto.setIdUser(usuario.getIdUser());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setApellidoUsuario(usuario.getApellidoUsuario());
        dto.setEmailUsuario(usuario.getEmailUsuario());
        return dto;
    }

    @Override
    public UserIdDTo userToIdDTO(User usuario) {
        UserIdDTo dto= new UserIdDTo();
        dto.setIdUser(usuario.getIdUser());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setApellidoUsuario(usuario.getApellidoUsuario());
        dto.setEmailUsuario(usuario.getEmailUsuario());
        dto.setActivo(usuario.getActivo());
        return dto;
    }
}
