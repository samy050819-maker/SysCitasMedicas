-- ============================================================================
-- 5_vpd_client_identifier.sql
-- Actualización de FN_FILTRO_HISTORIAS_MEDICO para usar CLIENT_IDENTIFIER
-- en lugar de SESSION_USER, compatible con pool HikariCP de Spring Boot.
-- ============================================================================
-- Ejecutar como SISTEMACITASMEDICAS o como SYS con ALTER SESSION SET CURRENT_SCHEMA.
-- ============================================================================

CREATE OR REPLACE FUNCTION SISTEMACITASMEDICAS.FN_FILTRO_HISTORIAS_MEDICO (
    p_schema IN VARCHAR2,
    p_table  IN VARCHAR2
) RETURN VARCHAR2
AS
    v_client_id    VARCHAR2(128);
    v_session_user VARCHAR2(128);
    v_id_entidad   NUMBER;
BEGIN
    -- 1. Leer CLIENT_IDENTIFIER (establecido por Spring Boot via DBMS_SESSION.SET_IDENTIFIER)
    v_client_id := UPPER(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'));

    -- 2. Leer SESSION_USER como fallback
    v_session_user := UPPER(SYS_CONTEXT('USERENV', 'SESSION_USER'));

    -- 3. Si hay CLIENT_IDENTIFIER establecido, aplicar filtro por usuario
    IF v_client_id IS NOT NULL THEN
        -- Verificar si el usuario es Administrador (ID_ENTIDAD = 104)
        BEGIN
            SELECT u.ID_ENTIDAD
            INTO   v_id_entidad
            FROM   TBL_USUARIOS u
            WHERE  UPPER(u.USUARIO) = v_client_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_id_entidad := NULL;
        END;

        -- Si es Administrador, no filtrar (ve todo)
        IF v_id_entidad = 104 THEN
            RETURN NULL;
        END IF;

        -- Para médicos: filtrar historias clínicas donde el médico de la cita
        -- coincida con el usuario identificado
        RETURN 'ID_MEDICO = (SELECT m.ID_MEDICO FROM TBL_MEDICOS m '
            || 'JOIN TBL_USUARIOS u ON m.ID_USUARIO = u.ID_USUARIO '
            || 'WHERE UPPER(u.USUARIO) = ''' || v_client_id || ''')';
    END IF;

    -- 4. Fallback: si no hay CLIENT_IDENTIFIER y SESSION_USER es el owner del esquema,
    --    no filtrar (compatibilidad con ejecución directa en SQL*Plus/SQL Developer)
    IF v_session_user = 'SISTEMACITASMEDICAS' THEN
        RETURN NULL;
    END IF;

    -- 5. Para cualquier otro caso, bloquear acceso
    RETURN '1=0';
END FN_FILTRO_HISTORIAS_MEDICO;
/
