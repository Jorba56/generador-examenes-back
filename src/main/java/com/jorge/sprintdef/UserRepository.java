package com.jorge.sprintdef;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findUsersByActivoIs(boolean activo);
}




