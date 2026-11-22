package com.syscitasmedicas.citas_backend.historia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoriaDTO {
    private Long idHistoria;
    private Long idCita;
    private Long idMedico;
    private String diagnostico;
    private String notas;
    private String fechaCita;
    private Long idPaciente;
    private String nombrePaciente;
    private String nombreMedico;
    private String medicoUsuario;
}
