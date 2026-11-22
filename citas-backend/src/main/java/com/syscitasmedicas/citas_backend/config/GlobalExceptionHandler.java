package com.syscitasmedicas.citas_backend.config;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<Integer> CONFLICT_CODES = Set.of(20001);
    private static final Set<Integer> NOT_FOUND_CODES = Set.of(20003, 20007, 20009);
    private static final Set<Integer> BAD_REQUEST_CODES = Set.of(20002, 20004, 20005, 20006, 20008, 20010, 20011);

    @ExceptionHandler({UncategorizedSQLException.class, DataAccessException.class})
    public ResponseEntity<OracleErrorResponse> handleOracleException(DataAccessException ex) {
        SQLException sqlEx = extractSQLException(ex);
        if (sqlEx != null) {
            int errorCode = sqlEx.getErrorCode();
            String cleanMessage = limpiarMensajeOracle(sqlEx.getMessage());

            HttpStatus status;
            if (CONFLICT_CODES.contains(errorCode)) {
                status = HttpStatus.CONFLICT;
            } else if (NOT_FOUND_CODES.contains(errorCode)) {
                status = HttpStatus.NOT_FOUND;
            } else if (BAD_REQUEST_CODES.contains(errorCode)) {
                status = HttpStatus.BAD_REQUEST;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            return ResponseEntity.status(status)
                    .body(new OracleErrorResponse(errorCode, cleanMessage));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OracleErrorResponse(0, ex.getMessage()));
    }

    private SQLException extractSQLException(DataAccessException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof SQLException sqlException) {
                return sqlException;
            }
            cause = cause.getCause();
        }
        return null;
    }

    private String limpiarMensajeOracle(String mensaje) {
        if (mensaje == null) return "Error desconocido";
        // Remove ORA-XXXXX: prefix
        String limpio = mensaje.replaceAll("ORA-\\d{5}:\\s*", "");
        // Remove ORA-06512 stack trace lines
        int idx = limpio.indexOf("\nORA-06512");
        if (idx > 0) {
            limpio = limpio.substring(0, idx);
        }
        return limpio.trim();
    }
}
