package com.example.chaea.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.chaea.dto.ProfesorDTO;
import com.example.chaea.entities.Profesor;
import com.example.chaea.entities.Rol;
import com.example.chaea.repositories.ProfesorRepository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/profesores")
public class ProfesorController {

    @Autowired
    private ProfesorRepository profesorRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @PostMapping
    public ResponseEntity<?> crearProfesor(@RequestBody ProfesorDTO profesorDTO) {
        // Validar campos requeridos
        if (profesorDTO.getEmail() == null || profesorDTO.getNombre() == null || profesorDTO.getCodigo() == null ||
            profesorDTO.getCarrera() == null || profesorDTO.getEstado() == null || profesorDTO.getEstadoProfesor() == null ||
            profesorDTO.getRolId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos requeridos.");
        }
        // Validar formato de correo electrónico
        if (!EMAIL_PATTERN.matcher(profesorDTO.getEmail()).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + profesorDTO.getEmail());
        }
        // Verificar si el correo ya existe
        if (profesorRepository.existsById(profesorDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Profesor con el correo " + profesorDTO.getEmail() + " ya existe.");
        }

        Rol rol = obtenerRolPorId(profesorDTO.getRolId());
        Profesor profesor = new Profesor(
            profesorDTO.getEmail(),
            profesorDTO.getNombre(),
            profesorDTO.getCodigo(),
            profesorDTO.getCarrera(),
            profesorDTO.getEstado(),
            profesorDTO.getEstadoProfesor()
        );
        profesor.setRol(rol);
        return ResponseEntity.ok(profesorRepository.save(profesor));
    }

    @GetMapping
    public ResponseEntity<List<Profesor>> listarProfesores() {
        return ResponseEntity.ok(profesorRepository.findAll());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> consultarPorCorreo(@PathVariable String email) {
        // Validar formato de correo electrónico
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
        }
        Optional<Profesor> profesorOptional = profesorRepository.findById(email);
        if (!profesorOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profesor no encontrado con el correo: " + email);
        }
        return ResponseEntity.ok(profesorOptional.get());
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> eliminarProfesor(@PathVariable String email) {
        // Validar formato de correo electrónico
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
        }
        Optional<Profesor> profesorOptional = profesorRepository.findById(email);
        if (!profesorOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profesor no encontrado con el correo: " + email);
        }
        profesorRepository.deleteById(email);
        return ResponseEntity.ok().body("Profesor eliminado exitosamente.");
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> actualizarProfesor(@PathVariable String email, @RequestBody ProfesorDTO profesorDTO) {
        // Validar formato de correo electrónico
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
        }
        Optional<Profesor> profesorOptional = profesorRepository.findById(email);
        if (!profesorOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profesor no encontrado con el correo: " + email);
        }
        Profesor profesorExistente = profesorOptional.get();
        profesorExistente.setNombre(profesorDTO.getNombre());
        profesorExistente.setCodigo(profesorDTO.getCodigo());
        profesorExistente.setCarrera(profesorDTO.getCarrera());
        profesorExistente.setRol(obtenerRolPorId(profesorDTO.getRolId()));
        profesorExistente.setEstadoProfesor(profesorDTO.getEstadoProfesor());
        profesorExistente.setEstado(profesorDTO.getEstado());
        return ResponseEntity.ok(profesorRepository.save(profesorExistente));
    }

    private Rol obtenerRolPorId(int rolId) {
        switch (rolId) {
            case 1:
                return new Rol(1, "Profesor Admin");
            case 2:
                return new Rol(2, "Profesor Normal");
            case 3:
                return new Rol(3, "Usuario");
            default:
                throw new IllegalArgumentException("Rol ID inválido: " + rolId);
        }
    }
}