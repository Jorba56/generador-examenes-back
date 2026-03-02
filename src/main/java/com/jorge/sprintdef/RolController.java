package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.dto.RolPutDTO;
import com.jorge.sprintdef.dto.UserByRol;
import com.jorge.sprintdef.services.RolService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST encargado de gestionar las peticiones HTTP relacionadas con los Roles del sistema.
 * Permite listar, crear, modificar y eliminar los distintos niveles de acceso (roles).
 */
@RestController
@RequestMapping("/roles")
public class RolController{

    private final RolService rolService;


    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    /**
     * Obtiene el listado de todos los roles disponibles y activos.
     */
    @GetMapping
    public List<Rol> getAllRoles(){
        return rolService.listarRoles();
    }

    /**
     * Busca un rol específico por su identificador.
     */
    @GetMapping("/{id}")
    public Optional<Rol> getRolId (@PathVariable Long id){
        return rolService.rolPorId(id);
    }

    /**
     * Crea un nuevo rol en el sistema (por ejemplo, "ADMIN" o "PROFESOR").
     */
    @PostMapping("")
    public String addRol (@RequestBody RolDTO rol){
        return rolService.newRol(rol);
    }

    /**
     * Modifica el nombre o el estado de un rol existente.
     */
    @PutMapping("/{id}")
    public String updateRol(@PathVariable Long id, @RequestBody RolPutDTO rolNuevo) {
        return rolService.actualizarRol(id, rolNuevo);
    }

    /**
     * Realiza el borrado lógico de un rol para que deje de estar disponible.
     */
    @DeleteMapping("/{id}")
    public String deleteRol (@PathVariable Long id){
        return rolService.desactivarRol(id);
    }

    /**
     * Consulta qué usuarios tienen asignado el rol especificado.
     */
    @GetMapping("/{idRol}/usuarios")
    public List<UserByRol> userRol(@PathVariable Long idRol){
        return rolService.userPorRol(idRol);
    }

}
