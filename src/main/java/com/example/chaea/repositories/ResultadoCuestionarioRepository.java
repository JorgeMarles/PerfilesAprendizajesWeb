package com.example.chaea.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chaea.entities.Cuestionario;
import com.example.chaea.entities.Estudiante;
import com.example.chaea.entities.ResultadoCuestionario;

public interface ResultadoCuestionarioRepository extends JpaRepository<ResultadoCuestionario, Long>{
 // Método para encontrar un ResultadoCuestionario por Cuestionario, Estudiante y fechaResolucion null 
    Optional<ResultadoCuestionario> findByCuestionarioAndEstudianteAndFechaResolucionIsNull(Cuestionario cuestionario, Estudiante estudiante); 
    // Método para encontrar una lista de ResultadoCuestionario por Estudiante y fechaResolucion null 
    List<ResultadoCuestionario> findByEstudianteAndFechaResolucionIsNull(Estudiante estudiante);
}