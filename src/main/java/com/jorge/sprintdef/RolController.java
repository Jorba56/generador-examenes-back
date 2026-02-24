package com.jorge.sprintdef;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users/roles")
public class RolController{

    private final RolRepository rolRepository;

    public RolController(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @GetMapping
    public List<Rol> getAllRoles(){
        return rolRepository.findRolsByActivoIs(true);
    }

    @GetMapping("/{id}")
    public Optional<Rol> getRolId (@PathVariable Long id){
        return rolRepository.findById(id);
    }

    @PostMapping("")
    public String addRol (@RequestBody Rol rol){
        rolRepository.save(rol);
        return("rol añadido con exito");
    }

    @PutMapping("/update/{id}")
    public String updateRol(@PathVariable Long id, @RequestBody Rol rolNuevo) {

        // 1. Buscamos el usuario y abrimos el Optional de forma segura
        Rol rolUpdate = rolRepository.findById(id).orElse(null);

        // 2. Comprobamos que exista
        if (rolUpdate == null) {
            return "Error: Usuario no encontrado";
        }

        // 3. Actualizamos los datos
        rolUpdate.setName(rolNuevo.getName());
        rolUpdate.setActivo(rolNuevo.getActivo());

        // 5. Guardamos en la base de datos
        rolRepository.save(rolUpdate);

        return "Rol editado correctamente";
    }

    @DeleteMapping("/{id}")
    public String deleteRol (@PathVariable Long id){
        Rol rolSelect=rolRepository.findById(id).orElse(null);
        if (rolSelect!=null) {
            rolSelect.setActivo(false);
            rolRepository.save(rolSelect);
        }
        return("rol borrado con exito");
    }

}
