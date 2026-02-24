package com.jorge.sprintdef;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/hola")
public class HolaMundo {
    @GetMapping
    public String saludo(){
        return "hola mundo";
    }

}
