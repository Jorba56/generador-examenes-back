package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.dto.RolPutDTO;
import com.jorge.sprintdef.services.RolService;
import com.jorge.sprintdef.services.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios/roles")
public class RolController{

    private final RolService rolService;

    private UserService userService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<Rol> getAllRoles(){
        return rolService.listarRoles();
    }

    @GetMapping("/{id}")
    public Optional<Rol> getRolId (@PathVariable Long id){
        return rolService.rolPorId(id);
    }

    @PostMapping("")
    public String addRol (@RequestBody RolDTO rol){
        return rolService.newRol(rol);
    }

    @PutMapping("/update/{id}")
    public String updateRol(@PathVariable Long id, @RequestBody RolPutDTO rolNuevo) {
        return rolService.actualizarRol(id, rolNuevo);
    }

    @DeleteMapping("/{id}")
    public String deleteRol (@PathVariable Long id){
        return rolService.desactivarRol(id);
    }

    @GetMapping("/{id_rol}/usuarios")
    public List<User> userRol(@PathVariable Long id_rol){
        return rolService.userPorRol(id_rol);
    }

}
