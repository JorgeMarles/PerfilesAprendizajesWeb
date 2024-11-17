package com.example.chaea.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Opcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "pregunta_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Pregunta pregunta;
    
    @ManyToOne
    @JoinColumn(name = "categoria_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Categoria categoria;
    
    @Column(nullable = false)
    private String respuesta;
    
    @Column(nullable = false)
    private Double valor;
    
    @Column(nullable = false)
    private int orden;
}
