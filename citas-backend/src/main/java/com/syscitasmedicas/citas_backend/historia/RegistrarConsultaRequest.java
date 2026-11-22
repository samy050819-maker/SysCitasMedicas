package com.syscitasmedicas.citas_backend.historia;

import lombok.Data;

@Data
public class RegistrarConsultaRequest {
    private Long idCita;
    private String diagnostico;
    private String notas;
}
