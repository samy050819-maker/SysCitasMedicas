package com.syscitasmedicas.citas_backend.historia;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HistoriaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public HistoriaRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Consulta TBL_HISTORIAS_CLINICAS aplicando la política de seguridad VPD (HU07)
     * mediante CLIENT_IDENTIFIER sobre el pool HikariCP.
     */
    public List<HistoriaDTO> findHistorias(String medicoUsuario) {
        return jdbcTemplate.execute((ConnectionCallback<List<HistoriaDTO>>) conn -> {
            boolean identifierSet = false;
            try {
                if (medicoUsuario != null && !medicoUsuario.trim().isEmpty()) {
                    try (CallableStatement cs = conn.prepareCall("{call DBMS_SESSION.SET_IDENTIFIER(?)}")) {
                        cs.setString(1, medicoUsuario.trim());
                        cs.execute();
                        identifierSet = true;
                    }
                }

                String sql = """
                        SELECT h.ID_HISTORIA,
                               h.ID_CITA,
                               h.ID_MEDICO,
                               h.DIAGNOSTICO,
                               h.NOTAS,
                               TO_CHAR(c.FECHA_HORA_INICIO, 'YYYY-MM-DD"T"HH24:MI:SS') AS FECHA_CITA,
                               p.ID_PACIENTE,
                               p.NOMBRES || ' ' || p.APELLIDOS AS NOMBRE_PACIENTE,
                               m.NOMBRES || ' ' || m.APELLIDOS AS NOMBRE_MEDICO,
                               u.USUARIO AS MEDICO_USUARIO
                        FROM TBL_HISTORIAS_CLINICAS h
                        JOIN TBL_CITAS c ON h.ID_CITA = c.ID_CITA
                        JOIN TBL_PACIENTES p ON c.ID_PACIENTE = p.ID_PACIENTE
                        JOIN TBL_MEDICOS m ON h.ID_MEDICO = m.ID_MEDICO
                        JOIN TBL_USUARIOS u ON m.ID_USUARIO = u.ID_USUARIO
                        ORDER BY h.ID_HISTORIA DESC
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    List<HistoriaDTO> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(new HistoriaDTO(
                                rs.getLong("ID_HISTORIA"),
                                rs.getLong("ID_CITA"),
                                rs.getLong("ID_MEDICO"),
                                rs.getString("DIAGNOSTICO"),
                                rs.getString("NOTAS"),
                                rs.getString("FECHA_CITA"),
                                rs.getLong("ID_PACIENTE"),
                                rs.getString("NOMBRE_PACIENTE"),
                                rs.getString("NOMBRE_MEDICO"),
                                rs.getString("MEDICO_USUARIO")
                        ));
                    }
                    return list;
                }
            } finally {
                if (identifierSet) {
                    try (CallableStatement cs = conn.prepareCall("{call DBMS_SESSION.CLEAR_IDENTIFIER}")) {
                        cs.execute();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    /**
     * PKG_HISTORIA.PRC_REGISTRAR_CONSULTA
     */
    public void registrarConsulta(Long idCita, String diagnostico, String notas) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_HISTORIA")
                .withProcedureName("PRC_REGISTRAR_CONSULTA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_CITA", Types.NUMERIC),
                        new SqlParameter("P_DIAGNOSTICO", Types.VARCHAR),
                        new SqlParameter("P_NOTAS", Types.VARCHAR)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("P_ID_CITA", idCita);
        params.put("P_DIAGNOSTICO", diagnostico);
        params.put("P_NOTAS", notas);

        call.execute(params);
    }
}
