package com.syscitasmedicas.citas_backend.citas;

import com.syscitasmedicas.citas_backend.citas.dto.CitaDTO;
import com.syscitasmedicas.citas_backend.citas.dto.CuposDTO;
import com.syscitasmedicas.citas_backend.citas.dto.HorarioDisponibleDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class CitaRepository {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public CitaRepository(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CitaDTO> citaRowMapper = (rs, rowNum) -> new CitaDTO(
            rs.getLong("ID_CITA"),
            rs.getLong("ID_ESTADO"),
            rs.getString("ESTADO"),
            rs.getString("NOMBRE_PACIENTE"),
            rs.getString("NOMBRE_MEDICO"),
            rs.getString("ESPECIALIDAD"),
            rs.getString("FECHA_HORA_INICIO"),
            rs.getString("FECHA_HORA_FIN")
    );

    public List<CitaDTO> findAll(Long idEstado) {
        if (idEstado != null) {
            String sql = """
                    SELECT c.ID_CITA,
                           c.ID_ESTADO,
                           p_est.VALOR AS ESTADO,
                           p.NOMBRES || ' ' || p.APELLIDOS AS NOMBRE_PACIENTE,
                           m.NOMBRES || ' ' || m.APELLIDOS AS NOMBRE_MEDICO,
                           e.NOMBRE AS ESPECIALIDAD,
                           TO_CHAR(c.FECHA_HORA_INICIO, 'YYYY-MM-DD"T"HH24:MI:SS') AS FECHA_HORA_INICIO,
                           TO_CHAR(c.FECHA_HORA_FIN, 'YYYY-MM-DD"T"HH24:MI:SS') AS FECHA_HORA_FIN
                    FROM TBL_CITAS c
                    JOIN TBL_PARAMETROS p_est ON c.ID_ESTADO = p_est.ID_PARAMETRO
                    JOIN TBL_PACIENTES p ON c.ID_PACIENTE = p.ID_PACIENTE
                    JOIN TBL_MEDICOS m ON c.ID_MEDICO = m.ID_MEDICO
                    JOIN TBL_ESPECIALIDADES e ON m.ID_ESPECIALIDAD = e.ID_ESPECIALIDAD
                    WHERE c.ID_ESTADO = ?
                    ORDER BY c.FECHA_HORA_INICIO DESC
                    """;
            return jdbcTemplate.query(sql, citaRowMapper, idEstado);
        } else {
            String sql = """
                    SELECT c.ID_CITA,
                           c.ID_ESTADO,
                           p_est.VALOR AS ESTADO,
                           p.NOMBRES || ' ' || p.APELLIDOS AS NOMBRE_PACIENTE,
                           m.NOMBRES || ' ' || m.APELLIDOS AS NOMBRE_MEDICO,
                           e.NOMBRE AS ESPECIALIDAD,
                           TO_CHAR(c.FECHA_HORA_INICIO, 'YYYY-MM-DD"T"HH24:MI:SS') AS FECHA_HORA_INICIO,
                           TO_CHAR(c.FECHA_HORA_FIN, 'YYYY-MM-DD"T"HH24:MI:SS') AS FECHA_HORA_FIN
                    FROM TBL_CITAS c
                    JOIN TBL_PARAMETROS p_est ON c.ID_ESTADO = p_est.ID_PARAMETRO
                    JOIN TBL_PACIENTES p ON c.ID_PACIENTE = p.ID_PACIENTE
                    JOIN TBL_MEDICOS m ON c.ID_MEDICO = m.ID_MEDICO
                    JOIN TBL_ESPECIALIDADES e ON m.ID_ESPECIALIDAD = e.ID_ESPECIALIDAD
                    ORDER BY c.FECHA_HORA_INICIO DESC
                    """;
            return jdbcTemplate.query(sql, citaRowMapper);
        }
    }

    /**
     * PRC_OBTENER_HORARIOS_DISPONIBLES -> SYS_REFCURSOR
     */
    @SuppressWarnings("unchecked")
    public List<HorarioDisponibleDTO> obtenerHorariosDisponibles(Long idMedico, LocalDate fecha, int duracionMin) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_CITAS")
                .withProcedureName("PRC_OBTENER_HORARIOS_DISPONIBLES")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_MEDICO", Types.NUMERIC),
                        new SqlParameter("P_FECHA", Types.DATE),
                        new SqlParameter("P_DURACION_MIN", Types.NUMERIC),
                        new SqlOutParameter("P_CURSOR", Types.REF_CURSOR,
                                (rs, rowNum) -> new HorarioDisponibleDTO(
                                        rs.getLong("ID_AGENDA"),
                                        rs.getString("FECHA_HORA_INICIO"),
                                        rs.getString("FECHA_HORA_FIN"),
                                        rs.getString("ETIQUETA_DESPLEGABLE")
                                ))
                );

        Map<String, Object> params = Map.of(
                "P_ID_MEDICO", idMedico,
                "P_FECHA", java.sql.Date.valueOf(fecha),
                "P_DURACION_MIN", duracionMin
        );

        Map<String, Object> result = call.execute(params);
        return (List<HorarioDisponibleDTO>) result.get("P_CURSOR");
    }

    /**
     * PRC_CALCULAR_CUPOS_BLOQUE -> OUT P_TOTAL_CUPOS, P_CUPOS_DISPONIBLES
     */
    public CuposDTO calcularCuposBloque(Long idAgenda, int duracionMin) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_CITAS")
                .withProcedureName("PRC_CALCULAR_CUPOS_BLOQUE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_AGENDA", Types.NUMERIC),
                        new SqlParameter("P_DURACION_MIN", Types.NUMERIC),
                        new SqlOutParameter("P_TOTAL_CUPOS", Types.NUMERIC),
                        new SqlOutParameter("P_CUPOS_DISPONIBLES", Types.NUMERIC)
                );

        Map<String, Object> params = Map.of(
                "P_ID_AGENDA", idAgenda,
                "P_DURACION_MIN", duracionMin
        );

        Map<String, Object> result = call.execute(params);
        int total = ((BigDecimal) result.get("P_TOTAL_CUPOS")).intValue();
        int disponibles = ((BigDecimal) result.get("P_CUPOS_DISPONIBLES")).intValue();
        return new CuposDTO(total, disponibles);
    }

    /**
     * PRC_RESERVAR_CITA
     */
    public void reservarCita(Long idPaciente, Long idMedico, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_CITAS")
                .withProcedureName("PRC_RESERVAR_CITA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_PACIENTE", Types.NUMERIC),
                        new SqlParameter("P_ID_MEDICO", Types.NUMERIC),
                        new SqlParameter("P_FECHA_HORA_INICIO", Types.TIMESTAMP),
                        new SqlParameter("P_FECHA_HORA_FIN", Types.TIMESTAMP)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("P_ID_PACIENTE", idPaciente);
        params.put("P_ID_MEDICO", idMedico);
        params.put("P_FECHA_HORA_INICIO", Timestamp.valueOf(fechaHoraInicio));
        params.put("P_FECHA_HORA_FIN", fechaHoraFin != null ? Timestamp.valueOf(fechaHoraFin) : null);

        call.execute(params);
    }

    /**
     * PRC_CANCELAR_CITA
     */
    public void cancelarCita(Long idCita) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_CITAS")
                .withProcedureName("PRC_CANCELAR_CITA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_CITA", Types.NUMERIC)
                );

        call.execute(Map.of("P_ID_CITA", idCita));
    }

    /**
     * PRC_CANCELACION_MASIVA
     */
    public void cancelacionMasiva(Long idMedico, LocalDate fecha) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_CITAS")
                .withProcedureName("PRC_CANCELACION_MASIVA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_MEDICO", Types.NUMERIC),
                        new SqlParameter("P_FECHA", Types.DATE)
                );

        call.execute(Map.of(
                "P_ID_MEDICO", idMedico,
                "P_FECHA", java.sql.Date.valueOf(fecha)
        ));
    }
}
