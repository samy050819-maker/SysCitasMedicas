package com.syscitasmedicas.citas_backend.farmacia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FarmaciaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public FarmaciaRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    private final RowMapper<MedicamentoDTO> medicamentoRowMapper = (rs, rowNum) -> new MedicamentoDTO(
            rs.getLong("ID_MEDICAMENTO"),
            rs.getInt("ID_TIPO_MEDICAMENTO"),
            rs.getString("NOMBRE"),
            rs.getString("DESCRIPCION"),
            rs.getString("PRESENTACION")
    );

    private final RowMapper<RecetaPendienteDTO> recetaRowMapper = (rs, rowNum) -> {
        Long idMed = rs.getObject("ID_MEDICAMENTO") != null ? rs.getLong("ID_MEDICAMENTO") : null;
        Integer cant = rs.getObject("CANTIDAD_RECETADA") != null ? rs.getInt("CANTIDAD_RECETADA") : null;
        return new RecetaPendienteDTO(
                rs.getLong("ID_RECETA"),
                rs.getObject("ID_CITA") != null ? rs.getLong("ID_CITA") : null,
                rs.getLong("ID_ESTADO_RECETA"),
                rs.getString("ESTADO_RECETA"),
                rs.getString("NOMBRE_PACIENTE"),
                rs.getString("NOMBRE_MEDICO"),
                idMed,
                rs.getString("NOMBRE_MEDICAMENTO"),
                cant,
                rs.getString("ESTADO_DETALLE")
        );
    };

    public List<MedicamentoDTO> findMedicamentos() {
        String sql = """
                SELECT m.ID_MEDICAMENTO,
                       m.ID_TIPO_MEDICAMENTO,
                       m.NOMBRE,
                       m.DESCRIPCION,
                       m.PRESENTACION
                FROM TBL_MEDICAMENTOS m
                ORDER BY m.NOMBRE
                """;
        return jdbcTemplate.query(sql, medicamentoRowMapper);
    }

    public List<RecetaPendienteDTO> findRecetasPendientes() {
        String sql = """
                SELECT r.ID_RECETA,
                       r.ID_CITA,
                       r.ID_ESTADO AS ID_ESTADO_RECETA,
                       p_est.VALOR AS ESTADO_RECETA,
                       p.NOMBRES || ' ' || p.APELLIDOS AS NOMBRE_PACIENTE,
                       m.NOMBRES || ' ' || m.APELLIDOS AS NOMBRE_MEDICO,
                       d.ID_MEDICAMENTO,
                       med.NOMBRE AS NOMBRE_MEDICAMENTO,
                       d.CANTIDAD AS CANTIDAD_RECETADA,
                       p_det.VALOR AS ESTADO_DETALLE
                FROM TBL_RECETAS_MEDICAS r
                JOIN TBL_PARAMETROS p_est ON r.ID_ESTADO = p_est.ID_PARAMETRO
                LEFT JOIN TBL_DETALLES_RECETAS d ON r.ID_RECETA = d.ID_RECETA
                LEFT JOIN TBL_MEDICAMENTOS med ON d.ID_MEDICAMENTO = med.ID_MEDICAMENTO
                LEFT JOIN TBL_PARAMETROS p_det ON d.ID_ESTADO = p_det.ID_PARAMETRO
                LEFT JOIN TBL_CITAS c ON r.ID_CITA = c.ID_CITA
                LEFT JOIN TBL_PACIENTES p ON c.ID_PACIENTE = p.ID_PACIENTE
                LEFT JOIN TBL_MEDICOS m ON c.ID_MEDICO = m.ID_MEDICO
                WHERE r.ID_ESTADO IN (601, 602)
                ORDER BY r.ID_RECETA DESC
                """;
        return jdbcTemplate.query(sql, recetaRowMapper);
    }

    /**
     * PKG_FARMACIA.PRC_DISPENSAR_MEDICAMENTO
     */
    public void dispensarMedicamento(Long idReceta, Long idMedicamento, int cantidad) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_FARMACIA")
                .withProcedureName("PRC_DISPENSAR_MEDICAMENTO")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_RECETA", Types.NUMERIC),
                        new SqlParameter("P_ID_MEDICAMENTO", Types.NUMERIC),
                        new SqlParameter("P_CANTIDAD", Types.NUMERIC)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("P_ID_RECETA", idReceta);
        params.put("P_ID_MEDICAMENTO", idMedicamento);
        params.put("P_CANTIDAD", cantidad);

        call.execute(params);
    }
}
