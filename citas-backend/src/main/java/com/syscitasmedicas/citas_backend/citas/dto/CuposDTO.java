package com.syscitasmedicas.citas_backend.citas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuposDTO {
    private int totalCupos;
    private int cuposDisponibles;
}
