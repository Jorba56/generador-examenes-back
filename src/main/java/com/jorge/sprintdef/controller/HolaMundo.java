package com.jorge.sprintdef.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/holamundo")
public class HolaMundo {
    @GetMapping
    public String saludo(){
        return "hola mundo";
    }
}
