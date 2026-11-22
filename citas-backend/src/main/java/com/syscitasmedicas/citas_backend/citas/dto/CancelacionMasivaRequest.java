package com.syscitasmedicas.citas_backend.citas.dto;

import lombok.Data;

@Data
public class CancelacionMasivaRequest {
    private Long idMedico;
    private String fecha;
}
