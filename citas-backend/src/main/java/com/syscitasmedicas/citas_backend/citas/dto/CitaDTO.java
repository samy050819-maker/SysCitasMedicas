package com.syscitasmedicas.citas_backend.citas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitaDTO {
    private Long idCita;
    private Long idEstado;
    private String estado;
    private String nombrePaciente;
    private String nombreMedico;
    private String especialidad;
    private String fechaHoraInicio;
    private String fechaHoraFin;
}
