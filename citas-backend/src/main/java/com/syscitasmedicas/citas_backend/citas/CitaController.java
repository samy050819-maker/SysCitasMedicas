package com.syscitasmedicas.citas_backend.citas;

import com.syscitasmedicas.citas_backend.citas.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaRepository citaRepository;

    public CitaController(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    /**
     * GET /api/citas?idEstado=501
     */
    @GetMapping
    public ResponseEntity<List<CitaDTO>> listarCitas(@RequestParam(required = false) Long idEstado) {
        return ResponseEntity.ok(citaRepository.findAll(idEstado));
    }

    /**
     * GET /api/citas/horarios-disponibles?idMedico=&fecha=&duracionMin=60
     */
    @GetMapping("/horarios-disponibles")
    public ResponseEntity<List<HorarioDisponibleDTO>> getHorariosDisponibles(
            @RequestParam Long idMedico,
            @RequestParam String fecha,
            @RequestParam(defaultValue = "60") int duracionMin) {

        LocalDate fechaLocal = LocalDate.parse(fecha);
        List<HorarioDisponibleDTO> horarios = citaRepository.obtenerHorariosDisponibles(idMedico, fechaLocal, duracionMin);
        return ResponseEntity.ok(horarios);
    }

    /**
     * GET /api/citas/cupos/{idAgenda}?duracionMin=60
     */
    @GetMapping("/cupos/{idAgenda}")
    public ResponseEntity<CuposDTO> getCupos(
            @PathVariable Long idAgenda,
            @RequestParam(defaultValue = "60") int duracionMin) {

        CuposDTO cupos = citaRepository.calcularCuposBloque(idAgenda, duracionMin);
        return ResponseEntity.ok(cupos);
    }

    /**
     * POST /api/citas/reservar
     */
    @PostMapping("/reservar")
    public ResponseEntity<Map<String, String>> reservarCita(@RequestBody ReservarCitaRequest request) {
        LocalDateTime inicio = LocalDateTime.parse(request.getFechaHoraInicio());
        LocalDateTime fin = request.getFechaHoraFin() != null
                ? LocalDateTime.parse(request.getFechaHoraFin())
                : null;

        citaRepository.reservarCita(request.getIdPaciente(), request.getIdMedico(), inicio, fin);
        return ResponseEntity.ok(Map.of("mensaje", "Cita reservada exitosamente"));
    }

    /**
     * PUT /api/citas/{idCita}/cancelar
     */
    @PutMapping("/{idCita}/cancelar")
    public ResponseEntity<Map<String, String>> cancelarCita(@PathVariable Long idCita) {
        citaRepository.cancelarCita(idCita);
        return ResponseEntity.ok(Map.of("mensaje", "Cita cancelada exitosamente"));
    }

    /**
     * POST /api/citas/cancelacion-masiva
     */
    @PostMapping("/cancelacion-masiva")
    public ResponseEntity<Map<String, String>> cancelacionMasiva(@RequestBody CancelacionMasivaRequest request) {
        LocalDate fecha = LocalDate.parse(request.getFecha());
        citaRepository.cancelacionMasiva(request.getIdMedico(), fecha);
        return ResponseEntity.ok(Map.of("mensaje", "Cancelación masiva ejecutada exitosamente"));
    }
}
