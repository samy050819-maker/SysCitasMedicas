package com.syscitasmedicas.citas_backend.parametros;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParametroDTO {
    private Long idParametro;
    private Integer idTipo;
    private String nombre;
    private String valor;
    private String descripcion;
}
