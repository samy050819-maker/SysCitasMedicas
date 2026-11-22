package com.syscitasmedicas.citas_backend.historia;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historias")
public class HistoriaController {

    private final HistoriaRepository historiaRepository;

    public HistoriaController(HistoriaRepository historiaRepository) {
        this.historiaRepository = historiaRepository;
    }

    /**
     * GET /api/historias (Header opcional X-Medico-Usuario)
     */
    @GetMapping
    public ResponseEntity<List<HistoriaDTO>> getHistorias(
            @RequestHeader(value = "X-Medico-Usuario", required = false) String medicoUsuario) {
        List<HistoriaDTO> historias = historiaRepository.findHistorias(medicoUsuario);
        return ResponseEntity.ok(historias);
    }

    /**
     * POST /api/historias
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> registrarConsulta(@RequestBody RegistrarConsultaRequest request) {
        historiaRepository.registrarConsulta(request.getIdCita(), request.getDiagnostico(), request.getNotas());
        return ResponseEntity.ok(Map.of("mensaje", "Consulta registrada exitosamente en historia clínica"));
    }
}
