package com.example.chaea.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chaea.entities.Cuestionario;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
    
}
