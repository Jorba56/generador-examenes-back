package com.jorge.sprintdef;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findRolsByActivoIs(boolean b);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.idRol = :idRol")
    List<User> findUsuariosPorRol(@Param("idRol") Long idRol);
}
