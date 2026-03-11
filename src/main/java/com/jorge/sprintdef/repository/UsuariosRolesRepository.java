package com.jorge.sprintdef.repository;

import com.jorge.sprintdef.entity.Rol;
import com.jorge.sprintdef.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosRolesRepository extends JpaRepository<Rol, User> {

}
