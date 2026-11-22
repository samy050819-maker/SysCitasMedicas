package com.syscitasmedicas.citas_backend.farmacia;

import lombok.Data;

@Data
public class DispensarRequest {
    private Long idReceta;
    private Long idMedicamento;
    private int cantidad;
}
