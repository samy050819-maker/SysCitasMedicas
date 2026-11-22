package com.syscitasmedicas.citas_backend.farmacia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaPendienteDTO {
    private Long idReceta;
    private Long idCita;
    private Long idEstadoReceta;
    private String estadoReceta;
    private String nombrePaciente;
    private String nombreMedico;
    private Long idMedicamento;
    private String nombreMedicamento;
    private Integer cantidadRecetada;
    private String estadoDetalle;
}
