package com.syscitasmedicas.citas_backend.pacientes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PacienteRepository {

    private final JdbcTemplate jdbcTemplate;

    public PacienteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PacienteDTO> rowMapper = (rs, rowNum) -> new PacienteDTO(
            rs.getLong("ID_PACIENTE"),
            rs.getString("DOCUMENTO"),
            rs.getString("NOMBRE_COMPLETO"),
            rs.getString("CORREO"),
            rs.getString("TELEFONO")
    );

    public List<PacienteDTO> findAllActivos() {
        String sql = """
                SELECT p.ID_PACIENTE,
                       u.USUARIO AS DOCUMENTO,
                       p.NOMBRES || ' ' || p.APELLIDOS AS NOMBRE_COMPLETO,
                       p.CORREO,
                       p.TELEFONO
                FROM TBL_PACIENTES p
                JOIN TBL_USUARIOS u ON p.ID_USUARIO = u.ID_USUARIO
                WHERE u.ID_ESTADO = 201
                ORDER BY p.APELLIDOS, p.NOMBRES
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }
}
