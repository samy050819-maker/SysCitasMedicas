package com.syscitasmedicas.citas_backend.medicos;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MedicoRepository {

    private final JdbcTemplate jdbcTemplate;

    public MedicoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicoDTO> rowMapper = (rs, rowNum) -> new MedicoDTO(
            rs.getLong("ID_MEDICO"),
            rs.getString("USUARIO"),
            rs.getString("NOMBRE_COMPLETO"),
            rs.getString("ESPECIALIDAD"),
            rs.getString("TELEFONO"),
            rs.getString("CORREO")
    );

    public List<MedicoDTO> findAllActivos() {
        String sql = """
                SELECT m.ID_MEDICO,
                       u.USUARIO,
                       m.NOMBRES || ' ' || m.APELLIDOS AS NOMBRE_COMPLETO,
                       e.NOMBRE AS ESPECIALIDAD,
                       m.TELEFONO,
                       m.CORREO
                FROM TBL_MEDICOS m
                JOIN TBL_ESPECIALIDADES e ON m.ID_ESPECIALIDAD = e.ID_ESPECIALIDAD
                JOIN TBL_USUARIOS u ON m.ID_USUARIO = u.ID_USUARIO
                WHERE u.ID_ESTADO = 201
                ORDER BY m.APELLIDOS, m.NOMBRES
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }
}
