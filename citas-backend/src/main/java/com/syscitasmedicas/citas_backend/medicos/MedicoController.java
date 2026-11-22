package com.syscitasmedicas.citas_backend.medicos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoRepository medicoRepository;

    public MedicoController(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @GetMapping
    public ResponseEntity<List<MedicoDTO>> listarMedicosActivos() {
        return ResponseEntity.ok(medicoRepository.findAllActivos());
    }
}
