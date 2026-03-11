package com.jorge.sprintdef.services;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.dto.RolPutDTO;
import com.jorge.sprintdef.dto.UserByRol;
import com.jorge.sprintdef.entity.Rol;
import com.jorge.sprintdef.exceptions.ConflictException;

import java.util.List;
import java.util.Optional;

public interface RolService {
    List<Rol> listarRoles();

    Optional<Rol> rolPorId(Long id);

    String newRol(RolDTO rol);

    String actualizarRol(Long id, RolPutDTO rolNuevo);

    String desactivarRol(Long id) throws ConflictException;

    Rol mappingARol(RolDTO rol);

    List<UserByRol> userPorRol(Long idRol);
}
