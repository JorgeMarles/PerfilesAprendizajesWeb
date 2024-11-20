package com.example.chaea.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chaea.dto.EstudianteCrearDTO;
import com.example.chaea.dto.GrupoDTO;
import com.example.chaea.entities.Estudiante;
import com.example.chaea.entities.Grupo;
import com.example.chaea.entities.Profesor;
import com.example.chaea.entities.UsuarioEstado;
import com.example.chaea.repositories.EstudianteRepository;
import com.example.chaea.repositories.GrupoRepository;
import com.example.chaea.repositories.ProfesorRepository;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {
    
    @Autowired
    private GrupoRepository grupoRepository;
    
    @Autowired
    private ProfesorRepository profesorRepository;
    
    @Autowired
    private EstudianteRepository estudianteRepository;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@ufps.edu.co$");
    
    @PostMapping
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> crearGrupo(@RequestBody GrupoDTO grupoDTO) {
        // Validar campos requeridos
    
        if (grupoDTO.getNombre() == null || grupoDTO.getEstudiantes() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos requeridos.");
        }
        
        // Buscar el profesor por su correo electrónico
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Crear una nueva instancia de Grupo
        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNombre(grupoDTO.getNombre());
        nuevoGrupo.setProfesor(profesor);
        // Guardar el nuevo grupo antes de asociar los estudiantes
        nuevoGrupo = grupoRepository.save(nuevoGrupo);
        // Crear un conjunto de estudiantes
        Set<Estudiante> estudiantesAsignados = new HashSet<>();

        if (grupoDTO.getEstudiantes() != null) {
            for (EstudianteCrearDTO estudiante : grupoDTO.getEstudiantes()) {
                String email = estudiante.getEmail();
                String nombre = estudiante.getNombre();
                Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
                if (estudianteOpt.isPresent()) {
                    Estudiante estud = estudianteOpt.get();
                    estudiantesAsignados.add(estud);
                } else {
                    Estudiante estud = new Estudiante();
                    estud.setEmail(email);
                    estud.setNombre(nombre);
                    estud.setEstado(UsuarioEstado.INCOMPLETA);
                    estudiantesAsignados.add(estud);
                }
            }
        }
        
        for (Estudiante estudiante : estudiantesAsignados) {
            estudiante.getGrupos().add(nuevoGrupo);
        }
        estudianteRepository.saveAll(estudiantesAsignados);
        // Asignar los estudiantes al grupo
        nuevoGrupo.setEstudiantes(estudiantesAsignados);
        // Actualizar el grupo con los estudiantes
        return ResponseEntity.ok(grupoRepository.save(nuevoGrupo));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Grupo>> listarGrupos() {
        // Buscar el profesor por su correo electrónico
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(grupoRepository.findByProfesor(profesor));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> consultarGrupoPorId(@PathVariable int id) {
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Grupo> grupoOpt = grupoRepository.findByProfesorAndId(profesor, id);
        if (!grupoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        return ResponseEntity.ok(grupoOpt.get());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarGrupo(@PathVariable int id) {
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Grupo> grupoOpt = grupoRepository.findByProfesorAndId(profesor, id);
        if (!grupoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOpt.get();
        // Desvincular los estudiantes del grupo
        for (Estudiante estudiante : grupo.getEstudiantes()) {
            estudiante.getGrupos().remove(grupo);
        }
        estudianteRepository.saveAll(grupo.getEstudiantes()); // Actualizar el estudiante
        // Ahora se puede eliminar el grupo
        grupoRepository.deleteById(id);
        return ResponseEntity.ok().body("Grupo eliminado exitosamente.");
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> actualizarGrupo(@PathVariable int id, @RequestBody GrupoDTO grupoDTO) {
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Grupo> grupoOptional = grupoRepository.findByProfesorAndId(profesor, id);
        if (!grupoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupoExistente = grupoOptional.get();
        grupoExistente.setNombre(grupoDTO.getNombre());
        
        // Actualizar los estudiantes
        Set<Estudiante> estudiantesAdd = new HashSet<>();
        for (EstudianteCrearDTO estud : grupoDTO.getEstudiantes()) {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(estud.getEmail());
            if (estudianteOpt.isPresent()) {
                Estudiante estudiante = estudianteOpt.get();
                if (!grupoExistente.getEstudiantes().contains(estudiante)) {
                    estudiante.getGrupos().add(grupoExistente);
                    estudiantesAdd.add(estudiante);
                }
            } else {
                Estudiante estudiante = new Estudiante();
                estudiante.setEmail(estud.getEmail());
                estudiante.setNombre(estud.getNombre());
                estudiante.setEstado(UsuarioEstado.INCOMPLETA);
                estudiantesAdd.add(estudiante);
            }
        }
        
        grupoExistente.getEstudiantes().addAll(estudiantesAdd);
        estudianteRepository.saveAll(estudiantesAdd);
        
        return ResponseEntity.ok(grupoRepository.save(grupoExistente));
    }
    
    // Método para agregar estudiantes a un grupo
    @PostMapping("/{id}/estudiantes")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> agregarEstudiantesAlGrupo(@PathVariable int id, @RequestBody List<String> emails) {
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Grupo> grupoOptional = grupoRepository.findByProfesorAndId(profesor, id);
        if (!grupoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOptional.get();
        List<String> emailsNoEncontrados = new ArrayList<>();
        Set<String> errorEmail = new HashSet<String>();
        // Validar formato de correos electrónicos
        for (String email : emails) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errorEmail.add(email);
            }
        }
        if (errorEmail.size() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Los siguientes correos no cumplen con el formato : " + errorEmail.toString());
        }
        Set<Estudiante> estudiantes = new HashSet<>();
        for (String email : emails) {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
            if (!estudianteOpt.isPresent()) {
                emailsNoEncontrados.add(email);
            } else {
                Estudiante estudiante = estudianteOpt.get();
                grupo.getEstudiantes().add(estudiante);
                estudiante.getGrupos().add(grupo);
                estudiantes.add(estudiante);
            }
        }
        
        if (!emailsNoEncontrados.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Estudiantes no encontrados con los correos: " + emailsNoEncontrados.toString());
        }
        estudianteRepository.saveAll(estudiantes);
        return ResponseEntity.ok(grupoRepository.save(grupo)); // Actualizar el grupo
    }
    
    // Método para eliminar UN estudiante de un grupo
    @DeleteMapping("/{id}/estudiantes/{email}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarEstudianteDelGrupo(@PathVariable int id, @PathVariable String email) {
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Grupo> grupoOptional = grupoRepository.findByProfesorAndId(profesor, id);
        if (!grupoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOptional.get();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de correo incorrecto: " + email);
        }
        Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
        if (!estudianteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Estudiante no encontrado con el correo: " + email);
        }
        Estudiante estudiante = estudianteOpt.get();
        grupo.getEstudiantes().remove(estudiante);
        estudiante.getGrupos().remove(grupo);
        estudianteRepository.save(estudiante);
        return ResponseEntity.ok(grupoRepository.save(grupo));
    }
    
    // Método para eliminar estudiantes de un grupo
    @DeleteMapping("/{id}/estudiantes")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarEstudiantesDelGrupo(@PathVariable int id, @RequestBody List<String> emails) {
        Profesor profesor = (Profesor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Grupo> grupoOptional = grupoRepository.findByProfesorAndId(profesor, id);
        if (!grupoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado con el ID: " + id);
        }
        Grupo grupo = grupoOptional.get();
        List<String> emailsNoEncontrados = new ArrayList<>();
        Set<String> errorEmail = new HashSet<String>();
        // Validar formato de correos electrónicos
        for (String email : emails) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errorEmail.add(email);
            }
        }
        if (errorEmail.size() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Los siguientes correos no cumplen con el formato : " + errorEmail.toString());
        }
        Set<Estudiante> estudiantes = new HashSet<>();
        for (String email : emails) {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(email);
            if (!estudianteOpt.isPresent()) {
                emailsNoEncontrados.add(email);
            } else {
                Estudiante estudiante = estudianteOpt.get();
                grupo.getEstudiantes().remove(estudiante);
                estudiante.getGrupos().remove(grupo);
                estudiantes.add(estudiante);
            }
        }
        estudianteRepository.saveAll(estudiantes);
        if (!emailsNoEncontrados.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Estudiantes no encontrados con los correos: " + emailsNoEncontrados.toString());
        }
        return ResponseEntity.ok(grupoRepository.save(grupo)); // Actualizar el grupo
    }
}