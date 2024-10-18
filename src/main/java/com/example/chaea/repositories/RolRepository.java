package com.example.chaea.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chaea.entities.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    
}
