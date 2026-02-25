package com.jorge.sprintdef.services;


import com.jorge.sprintdef.*;
import com.jorge.sprintdef.dto.RolDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<Rol>listarRoles(){
        return rolRepository.findRolsByActivoIs(true);
    }

    public Optional<Rol> rolPorId (Long id){
        return rolRepository.findById(id);
    }

    public String newRol (RolDTO rol){
        Rol rol2=mappingARol(rol);
        rolRepository.save(rol2);
        return("rol añadido con exito");
    }

    public String actualizarRol(Long id, Rol rolNuevo) {
         String salida;
        // 1. Buscamos el usuario y abrimos el Optional de forma segura
        Rol rolUpdate = rolRepository.findById(id).orElse(new Rol());

        // 2. Comprobamos que exista
        if (rolUpdate.getIdRol()==null) {
             salida= "Error: Rol no encontrado";
        }else {
            // 3.   Actualizamos los datos
            rolUpdate.setName(rolNuevo.getName());
            rolUpdate.setActivo(rolNuevo.getActivo());

            // 5.   Guardamos en la base de datos
            rolRepository.save(rolUpdate);
            salida= "Rol editado correctamente";
        }
        return salida;

    }

    public String desactivarRol ( Long id){
        Rol rolSelect=rolRepository.findById(id).orElse(null);
        if (rolSelect!=null) {
            rolSelect.setActivo(false);
            rolRepository.save(rolSelect);
        }
        return("rol borrado con exito");
    }

    public Rol mappingARol(RolDTO rol){
        Rol dto= new Rol();
        dto.setName(rol.getName());
        return dto;
    }

}
