package com.example.chaea.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chaea.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
}
