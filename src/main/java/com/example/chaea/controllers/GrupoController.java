package com.example.chaea.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.chaea.dto.GrupoDTO;
import com.example.chaea.entities.Estudiante;
import com.example.chaea.entities.Grupo;
import com.example.chaea.entities.Profesor;
import com.example.chaea.repositories.EstudianteRepository;
import com.example.chaea.repositories.GrupoRepository;
import com.example.chaea.repositories.ProfesorRepository;

import java.util.*;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @PostMapping
    public ResponseEntity<?> crearGrupo(@RequestBody GrupoDTO grupoDTO) {
        // Validar campos requeridos
        if (grupoDTO.getNombre() == null || grupoDTO.getProfesorEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos requeridos.");
        }
        // Verificar si el grupo ya existe con ese nombre
        if (!grupoRepository.findByNombre(grupoDTO.getNombre()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Grupo con el nombre " + grupoDTO.getNombre() + " ya existe.");
        }
        // Validar formato de correos electrónicos
        if (grupoDTO.getCorreosEstudiantes() != null) {
            for (String email : grupoDTO.getCorreosEstudiantes()) {
                if (!email.contains("@")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
                }
            }
        }
        // Buscar el profesor por su correo electrónico
        Optional<Profesor> profesorOpt = profesorRepository.findById(grupoDTO.getProfesorEmail());
        if (!profesorOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Profesor no encontrado con el correo: " + grupoDTO.getProfesorEmail());
        }
        // Crear una nueva instancia de Grupo
        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNombre(grupoDTO.getNombre());
        nuevoGrupo.setProfesor(profesorOpt.get());
        // Guardar el nuevo grupo antes de asociar los estudiantes
        nuevoGrupo = grupoRepository.save(nuevoGrupo);
        // Crear un conjunto de estudiantes
        Set<Estudiante> estudiantesAsignados = new HashSet<>();
        if (grupoDTO.getCorreosEstudiantes() != null) {
            for (String email : grupoDTO.getCorreosEstudiantes()) {
                Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
                if (estudianteOpt.isPresent()) {
                    Estudiante estudiante = estudianteOpt.get();
                    estudiantesAsignados.add(estudiante);
                    // Añadir el grupo al estudiante
                    estudiante.getGrupos().add(nuevoGrupo);
                    estudianteRepository.save(estudiante); // Actualizar el estudiante
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estudiante no encontrado con el correo: " + email);
                }
            }
        }
        // Asignar los estudiantes al grupo
        nuevoGrupo.setEstudiantes(estudiantesAsignados);
        // Actualizar el grupo con los estudiantes
        return ResponseEntity.ok(grupoRepository.save(nuevoGrupo));
    }
    
    @GetMapping
    public ResponseEntity<List<Grupo>> listarGrupos() {
        return ResponseEntity.ok(grupoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultarGrupoPorId(@PathVariable int id) {
        Optional<Grupo> grupoOpt = grupoRepository.findById(id);
        if (!grupoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        return ResponseEntity.ok(grupoOpt.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarGrupo(@PathVariable int id) {
        Optional<Grupo> grupoOpt = grupoRepository.findById(id);
        if (!grupoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOpt.get();
        // Desvincular los estudiantes del grupo
        for (Estudiante estudiante : grupo.getEstudiantes()) {
            estudiante.getGrupos().remove(grupo);
            estudianteRepository.save(estudiante); // Actualizar el estudiante
        }
        // Ahora se puede eliminar el grupo
        grupoRepository.deleteById(id);
        return ResponseEntity.ok().body("Grupo eliminado exitosamente.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarGrupo(@PathVariable int id, @RequestBody GrupoDTO grupoDTO) {
        Optional<Grupo> grupoOptional = grupoRepository.findById(id);
        if (!grupoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupoExistente = grupoOptional.get();
        grupoExistente.setNombre(grupoDTO.getNombre());

        // Validar formato de correos electrónicos
        for (String email : grupoDTO.getCorreosEstudiantes()) {
            if (!email.contains("@")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
            }
        }

        // Actualizar el profesor si es necesario
        Optional<Profesor> profesorOpt = profesorRepository.findById(grupoDTO.getProfesorEmail());
        if (profesorOpt.isPresent()) {
            grupoExistente.setProfesor(profesorOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Profesor no encontrado con el correo: " + grupoDTO.getProfesorEmail());
        }

        // Actualizar los estudiantes
        Set<Estudiante> estudiantesAsignados = new HashSet<>();
        for (String email : grupoDTO.getCorreosEstudiantes()) {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
            if (estudianteOpt.isPresent()) {
                Estudiante estudiante = estudianteOpt.get();
                estudiantesAsignados.add(estudiante);
                estudiante.getGrupos().add(grupoExistente);
                estudianteRepository.save(estudiante); // Actualizar el estudiante
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estudiante no encontrado con el correo: " + email);
            }
        }
        grupoExistente.setEstudiantes(estudiantesAsignados);

        return ResponseEntity.ok(grupoRepository.save(grupoExistente));
    }

 // Método para eliminar un grupo por nombre
    @DeleteMapping("/nombre/{nombre}")
    public ResponseEntity<?> eliminarGrupoPorNombre(@PathVariable String nombre) {
        List<Grupo> grupos = grupoRepository.findByNombre(nombre);
        if (grupos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el nombre: " + nombre);
        }
        for (Grupo grupo : grupos) {
            // Desvincular los estudiantes del grupo
            for (Estudiante estudiante : grupo.getEstudiantes()) {
                estudiante.getGrupos().remove(grupo);
                estudianteRepository.save(estudiante); // Actualizar el estudiante
            }
            // Eliminar el grupo
            grupoRepository.delete(grupo);
        }
        return ResponseEntity.ok().body("Grupo(s) eliminado(s) exitosamente.");
    }

    // Método para agregar estudiantes a un grupo
    @PostMapping("/{id}/estudiantes")
    public ResponseEntity<?> agregarEstudiantesAlGrupo(@PathVariable int id, @RequestBody List<String> emails) {
        Optional<Grupo> grupoOpt = grupoRepository.findById(id);
        if (!grupoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOpt.get();
        List<String> emailsNoEncontrados = new ArrayList<>();
        for (String email : emails) {
            if (!email.contains("@")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
            }
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
            if (!estudianteOpt.isPresent()) {
                emailsNoEncontrados.add(email);
            } else {
                Estudiante estudiante = estudianteOpt.get();
                grupo.getEstudiantes().add(estudiante);
                estudiante.getGrupos().add(grupo);
                estudianteRepository.save(estudiante); // Actualizar el estudiante
            }
        }
        if (!emailsNoEncontrados.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estudiantes no encontrados con los correos: " + String.join(", ", emailsNoEncontrados));
        }
        return ResponseEntity.ok(grupoRepository.save(grupo)); // Actualizar el grupo
    }

    // Método para eliminar estudiantes de un grupo
    @DeleteMapping("/{id}/estudiantes")
    public ResponseEntity<?> eliminarEstudiantesDelGrupo(@PathVariable int id, @RequestBody List<String> emails) {
        Optional<Grupo> grupoOpt = grupoRepository.findById(id);
        if (!grupoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOpt.get();
        for (String email : emails) {
            if (!email.contains("@")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
            }
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
            if (!estudianteOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estudiante no encontrado con el correo: " + email);
            }
            Estudiante estudiante = estudianteOpt.get();
            grupo.getEstudiantes().remove(estudiante);
            estudiante.getGrupos().remove(grupo);
            estudianteRepository.save(estudiante); 
        }
        return ResponseEntity.ok(grupoRepository.save(grupo)); // Actualizar el grupo
    }
}