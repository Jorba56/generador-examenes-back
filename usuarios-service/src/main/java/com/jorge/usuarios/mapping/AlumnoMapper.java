package com.jorge.usuarios.mapping;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlumnoMapper {

    @Mapping(source = "idUser", target = "id")
    @Mapping(source = "nombreUsuario", target = "nombre")
    @Mapping(source = "apellidoUsuario", target = "apellidos")
    @Mapping(source = "emailUsuario", target = "correo")
    AlumnoDTO toAlumnoDTO(User user);

    List<AlumnoDTO> toAlumnoDTOList(List<User> users);
}