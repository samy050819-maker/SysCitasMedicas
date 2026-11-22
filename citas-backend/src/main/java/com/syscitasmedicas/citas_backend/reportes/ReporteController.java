package com.syscitasmedicas.citas_backend.reportes;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteRepository reporteRepository;

    public ReporteController(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    /**
     * GET /api/reportes/medicos?fechaInicio=&fechaFin=
     */
    @GetMapping("/medicos")
    public ResponseEntity<List<ReporteMedicoDTO>> getReporteMedicos(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);
        List<ReporteMedicoDTO> reporte = reporteRepository.reporteMedicosCursor(inicio, fin);
        return ResponseEntity.ok(reporte);
    }
}
