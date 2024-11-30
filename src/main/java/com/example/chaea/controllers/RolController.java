package com.example.chaea.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chaea.entities.Rol;
import com.example.chaea.repositories.RolRepository;

@RestController
@RequestMapping("/api/roles")
public class RolController {
    
    @Autowired
    private RolRepository rolRepository;
    
    // Crear un nuevo rol
    @PostMapping
    public ResponseEntity<Rol> crearRol(@RequestBody Rol rol) {
        Rol nuevoRol = rolRepository.save(rol);
        return ResponseEntity.ok(nuevoRol);
    }
    
    // Ver todos los roles
    @GetMapping
    public ResponseEntity<List<Rol>> verRoles() {
        List<Rol> roles = rolRepository.findAll();
        if (roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(roles);
    }
    
    // Borrar rol por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarRolPorId(@PathVariable int id) {
        Optional<Rol> rolOpt = rolRepository.findById(id);
        if (!rolOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        rolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}