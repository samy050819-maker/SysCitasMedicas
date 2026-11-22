package com.syscitasmedicas.citas_backend.farmacia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentoDTO {
    private Long idMedicamento;
    private Integer idTipoMedicamento;
    private String nombre;
    private String descripcion;
    private String presentacion;
}
