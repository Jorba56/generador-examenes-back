package com.jorge.sprintdef;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}

/*
    public UserRepository() {
        Users.add(new User("jbarrigar", "1234"));
        Users.add(new User("mariacv", "5678"));
        Users.add(new User("jose", "9999"));
    }

    public List<User> findAll() {
        return Users;
    }





    public void add(User usuario) {
        Users.add(usuario);
    }

    public void delete(int id) {
        Users.removeIf(user -> user.getId()==id);
    }
}*/



