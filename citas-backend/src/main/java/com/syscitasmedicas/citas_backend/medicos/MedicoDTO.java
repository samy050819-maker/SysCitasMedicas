package com.syscitasmedicas.citas_backend.medicos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicoDTO {
    private Long idMedico;
    private String usuario;
    private String nombreCompleto;
    private String especialidad;
    private String telefono;
    private String correo;
}
