package com.syscitasmedicas.citas_backend.citas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HorarioDisponibleDTO {
    private Long idAgenda;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String etiquetaDesplegable;
}
