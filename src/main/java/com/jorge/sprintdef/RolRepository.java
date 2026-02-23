package com.jorge.sprintdef;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
}
