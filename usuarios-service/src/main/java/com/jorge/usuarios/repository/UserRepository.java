package com.jorge.usuarios.repository;

import com.jorge.usuarios.entity.Rol;
import com.jorge.usuarios.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findUsersByActivoIs(boolean activo);
    User findUserByEmailUsuario(String correo);
    List<User> findByRoles_Name(String nombreRol);
    Optional <User> findByEmailUsuarioAndRoles_Name(String emailUsuario, String rolesName);
}




