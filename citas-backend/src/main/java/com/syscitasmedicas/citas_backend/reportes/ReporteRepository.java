package com.syscitasmedicas.citas_backend.reportes;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class ReporteRepository {

    private final DataSource dataSource;

    public ReporteRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * PKG_REPORTES.PRC_REPORTE_MEDICOS_CURSOR -> SYS_REFCURSOR
     */
    @SuppressWarnings("unchecked")
    public List<ReporteMedicoDTO> reporteMedicosCursor(LocalDate fechaInicio, LocalDate fechaFin) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("SISTEMACITASMEDICAS")
                .withCatalogName("PKG_REPORTES")
                .withProcedureName("PRC_REPORTE_MEDICOS_CURSOR")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_FECHA_INICIO", Types.DATE),
                        new SqlParameter("P_FECHA_FIN", Types.DATE),
                        new SqlOutParameter("P_CURSOR", Types.REF_CURSOR,
                                (rs, rowNum) -> new ReporteMedicoDTO(
                                        rs.getLong("ID_MEDICO"),
                                        rs.getString("NOMBRE_MEDICO"),
                                        rs.getInt("TOTAL_CITAS"),
                                        rs.getInt("PROGRAMADAS"),
                                        rs.getInt("CONFIRMADAS"),
                                        rs.getInt("ATENDIDAS"),
                                        rs.getInt("CANCELADAS"),
                                        rs.getInt("NO_ASISTIO"),
                                        rs.getBigDecimal("PORCENTAJE_CANCELACION")
                                ))
                );

        Map<String, Object> params = Map.of(
                "P_FECHA_INICIO", java.sql.Date.valueOf(fechaInicio),
                "P_FECHA_FIN", java.sql.Date.valueOf(fechaFin)
        );

        Map<String, Object> result = call.execute(params);
        return (List<ReporteMedicoDTO>) result.get("P_CURSOR");
    }
}
