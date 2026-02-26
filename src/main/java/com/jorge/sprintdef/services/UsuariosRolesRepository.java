package com.jorge.sprintdef.services;

import com.jorge.sprintdef.Rol;
import com.jorge.sprintdef.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosRolesRepository extends JpaRepository<Rol, User> {

}
