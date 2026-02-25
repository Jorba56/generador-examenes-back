package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.services.RolService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users/roles")
public class RolController{

    private final RolService rolService;

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
    public String updateRol(@PathVariable Long id, @RequestBody Rol rolNuevo) {
        return rolService.actualizarRol(id, rolNuevo);
    }

    @DeleteMapping("/{id}")
    public String deleteRol (@PathVariable Long id){
        return rolService.desactivarRol(id);
    }
}
