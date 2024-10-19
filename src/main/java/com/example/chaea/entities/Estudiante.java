package com.example.chaea.entities;

import java.sql.Date;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Table(name = "estudiante")
public class Estudiante extends Usuario {

    @Enumerated
    @Nullable
    private Genero genero;

    @Nullable
    private Date fecha_nacimiento;

    @ManyToMany
    @JoinTable(
        name = "matricula",
        joinColumns = @JoinColumn(name = "estudiante_id"),
        inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    @JsonBackReference
    private Set<Grupo> grupos;

    public Estudiante() {
        super();
    }

    public Estudiante(String email, String nombre, String codigo, Genero genero, Date fechaNac, UsuarioEstado estado) {
        super(email, nombre, codigo, estado);
        this.genero = genero;
        this.fecha_nacimiento = fechaNac;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estudiante)) return false;
        Estudiante that = (Estudiante) o;
        return Objects.equals(getEmail(), that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }
}