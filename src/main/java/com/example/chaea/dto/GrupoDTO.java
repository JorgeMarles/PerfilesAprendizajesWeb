package com.example.chaea.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoDTO {
	
    private String nombre;
    
    private String profesorEmail;
    
    private Set<String> correosEstudiantes; // Usaremos los correos de los estudiantes
    
}