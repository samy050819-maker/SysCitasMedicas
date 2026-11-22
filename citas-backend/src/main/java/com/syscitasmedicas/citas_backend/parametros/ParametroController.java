package com.syscitasmedicas.citas_backend.parametros;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    private final ParametroRepository parametroRepository;

    public ParametroController(ParametroRepository parametroRepository) {
        this.parametroRepository = parametroRepository;
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<List<ParametroDTO>> getByNombre(@PathVariable String nombre) {
        List<ParametroDTO> parametros = parametroRepository.findByNombre(nombre);
        return ResponseEntity.ok(parametros);
    }
}
