package com.syscitasmedicas.citas_backend.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteMedicoDTO {
    private Long idMedico;
    private String nombreMedico;
    private int totalCitas;
    private int programadas;
    private int confirmadas;
    private int atendidas;
    private int canceladas;
    private int noAsistio;
    private BigDecimal porcentajeCancelacion;
}
