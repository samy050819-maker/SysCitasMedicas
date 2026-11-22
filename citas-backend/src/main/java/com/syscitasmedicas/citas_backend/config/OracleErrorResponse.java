package com.syscitasmedicas.citas_backend.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OracleErrorResponse {
    private int codigoOracle;
    private String mensaje;
}
