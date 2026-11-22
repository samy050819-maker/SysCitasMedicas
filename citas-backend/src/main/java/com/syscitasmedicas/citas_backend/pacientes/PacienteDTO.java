package com.syscitasmedicas.citas_backend.pacientes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO {
    private Long idPaciente;
    private String documento;
    private String nombreCompleto;
    private String correo;
    private String telefono;
}
