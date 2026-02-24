package com.jorge.sprintdef;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findRolsByActivoIs(boolean b);

    List<Rol> getRolsByActivoIsTrue(boolean activo);
}
