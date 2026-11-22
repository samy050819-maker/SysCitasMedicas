package com.syscitasmedicas.citas_backend.citas.dto;

import lombok.Data;

@Data
public class ReservarCitaRequest {
    private Long idPaciente;
    private Long idMedico;
    private String fechaHoraInicio;
    private String fechaHoraFin;
}
