package com.syscitasmedicas.citas_backend.parametros;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParametroRepository {

    private final JdbcTemplate jdbcTemplate;

    public ParametroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ParametroDTO> rowMapper = (rs, rowNum) -> new ParametroDTO(
            rs.getLong("ID_PARAMETRO"),
            rs.getInt("ID_TIPO"),
            rs.getString("NOMBRE"),
            rs.getString("VALOR"),
            rs.getString("DESCRIPCION")
    );

    public List<ParametroDTO> findByNombre(String nombre) {
        String sql = "SELECT ID_PARAMETRO, ID_TIPO, NOMBRE, VALOR, DESCRIPCION " +
                     "FROM TBL_PARAMETROS WHERE UPPER(NOMBRE) = UPPER(?) ORDER BY ID_PARAMETRO";
        return jdbcTemplate.query(sql, rowMapper, nombre);
    }
}
