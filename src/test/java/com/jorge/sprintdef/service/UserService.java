package com.jorge.sprintdef.service;

import com.jorge.sprintdef.User;
import com.jorge.sprintdef.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userrep;

    public List<User> getUsers(){
        return userrep.findAll();
    }

    //puede devolver el usuario o no, dependiendo de si lo encuentra.
    public Optional<User> getUsersById(Long id){
        return userrep.findById(id);
    }

    public void addUser(User newUser){
         userrep.save(newUser);
    }
    public void delete(long id){
        userrep.deleteById(id);
    }


}
