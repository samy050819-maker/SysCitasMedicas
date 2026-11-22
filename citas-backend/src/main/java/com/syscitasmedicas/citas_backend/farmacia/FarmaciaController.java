package com.syscitasmedicas.citas_backend.farmacia;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmacia")
public class FarmaciaController {

    private final FarmaciaRepository farmaciaRepository;

    public FarmaciaController(FarmaciaRepository farmaciaRepository) {
        this.farmaciaRepository = farmaciaRepository;
    }

    /**
     * GET /api/farmacia/medicamentos
     */
    @GetMapping("/medicamentos")
    public ResponseEntity<List<MedicamentoDTO>> getMedicamentos() {
        return ResponseEntity.ok(farmaciaRepository.findMedicamentos());
    }

    /**
     * GET /api/farmacia/recetas-pendientes
     */
    @GetMapping("/recetas-pendientes")
    public ResponseEntity<List<RecetaPendienteDTO>> getRecetasPendientes() {
        return ResponseEntity.ok(farmaciaRepository.findRecetasPendientes());
    }

    /**
     * POST /api/farmacia/dispensar
     */
    @PostMapping("/dispensar")
    public ResponseEntity<Map<String, String>> dispensarMedicamento(@RequestBody DispensarRequest request) {
        farmaciaRepository.dispensarMedicamento(request.getIdReceta(), request.getIdMedicamento(), request.getCantidad());
        return ResponseEntity.ok(Map.of("mensaje", "Medicamento dispensado exitosamente"));
    }
}
