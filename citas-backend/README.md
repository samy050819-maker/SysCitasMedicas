\# Sistema de Gestión de Citas Médicas (Oracle PL/SQL + Spring Boot 3 + Vue 3)



\## 1. Contexto y Objetivo del Proyecto

Aplicación web Full-Stack para la gestión integral de citas médicas, historias clínicas, dispensación de farmacia y reportes gerenciales.



\*\*Principio arquitectónico fundamental:\*\* Toda la lógica transaccional crítica, validaciones de solapamiento, bloqueos pesimistas, auditoría y seguridad a nivel de fila (VPD) ya está implementada, compilada (`VALID`) y probada en \*\*Oracle Database 21c XE\*\* mediante paquetes PL/SQL y triggers. El backend en Spring Boot debe actuar como una \*\*API REST ligera orientada a llamadas PL/SQL (`Spring JDBC` / `SimpleJdbcCall`)\*\*, nunca reescribiendo lógica de negocio con ORM.



\---



\## 2. Stack Tecnológico y Reglas Estrictas



| Capa | Tecnología | Reglas obligatorias para implementación |

| :--- | :--- | :--- |

| \*\*Base de Datos\*\* | Oracle 21c XE (`SISTEMACITASMEDICAS`) | 16 tablas, 4 paquetes, 3 triggers y 1 función VPD en estado `VALID`. |

| \*\*Backend\*\* | Java 17/21 + Spring Boot 3 + Maven | Dependencias: `Spring Web`, `Spring JDBC`, `ojdbc11`, `Lombok`. \*\*PROHIBIDO usar Spring Data JPA / Hibernate\*\* para módulos transaccionales. Puerto: `8080`. |

| \*\*Frontend\*\* | Vue 3 (`<script setup>`) + Vite + Tailwind CSS + Axios | Agregar `Chart.js` (o `vue-chartjs`) para el dashboard de reportes (HU10). Puerto: `5173`. |

| \*\*CORS\*\* | `WebMvcConfigurer` en Spring Boot | Permitir `http://localhost:5173` para `/api/\*\*` (`GET, POST, PUT, DELETE, OPTIONS`). |



\---



\## 3. Cambios Recientes Aplicados a la Base de Datos (Estado Actual Real)



\### 3.1. Normalización de `TBL\_PARAMETROS` (43 registros)

La tabla paramétrica fue reestructurada para permitir consultas por categoría desde el frontend:

\- \*\*`ID\_PARAMETRO`\*\*: PK numérica que viaja como FK al resto del modelo (sin cambios).

\- \*\*`ID\_TIPO`\*\*: Identificador numérico de grupo (`1` al `9`).

\- \*\*`NOMBRE`\*\*: \*\*Categoría agrupadora\*\* (filtrar por esta columna para poblar desplegables).

\- \*\*`VALOR`\*\*: \*\*Texto legible para mostrar en UI\*\* (`Programada`, `Confirmada`, `Cancelada`, etc.).

\- \*\*`DESCRIPCION`\*\*: Sigla anterior entre corchetes + descripción documental.



\*\*Catálogo real vigente (`SELECT id\_parametro, id\_tipo, valor FROM tbl\_parametros WHERE nombre = ? ORDER BY id\_parametro`):\*\*

\- `TIPO\_ENTIDAD`: `101` Paciente, `102` Médico, `103` Farmacéutico, `104` Administrador.

\- `ESTADO\_USUARIO`: `201` Activo, `202` Inactivo, `203` Bloqueado.

\- `GENERO`: `301` Masculino, `302` Femenino, `303` Otro.

\- `ESTADO\_AGENDA`: `401` Disponible, `402` Ocupada, `403` Cancelada, `404` Vencida.

\- `ESTADO\_CITA`: `501` Programada, `502` Confirmada, `503` Atendida, `504` Cancelada, `505` No asistió.

\- `ESTADO\_RECETA`: `601` Emitida, `602` Dispensada parcial, `603` Dispensada, `604` Anulada.

\- `ESTADO\_DETALLE\_RECETA`: `701` Pendiente, `702` Dispensado parcial, `703` Dispensado, `704` Anulado.

\- `CATEGORIA\_MEDICAMENTO`: `801`–`810`.

\- `TIPO\_DOCUMENTO`: `901`–`906`.



\### 3.2. Migración 1:N en `TBL\_CITAS` (Médico -> Citas)

`TBL\_CITAS` dejó de depender de una relación 1:1 con `TBL\_AGENDAS\_MEDICOS`. Ahora un bloque de agenda (ej. 08:00–12:00) aloja múltiples citas de 1 hora sin restricción `UNIQUE` sobre `ID\_AGENDA`.



\*\*Estructura verificada de tablas principales:\*\*

\- \*\*`TBL\_CITAS`\*\*: `ID\_CITA NUMBER(8) PK`, `ID\_PACIENTE NUMBER(6) NOT NULL`, `ID\_AGENDA NUMBER(8) NULL`, `ID\_ESTADO NUMBER(6) NOT NULL`, `FECHA\_REGISTRO DATE NOT NULL`, `ID\_MEDICO NUMBER(6) NOT NULL FK`, `FECHA\_HORA\_INICIO TIMESTAMP(6) NOT NULL`, `FECHA\_HORA\_FIN TIMESTAMP(6) NOT NULL`.

&#x20; - Restricciones: `FK\_CITAS\_MEDICO`, `CHK\_CITAS\_RANGO\_HORAS (fecha\_hora\_fin > fecha\_hora\_inicio)`, índice `IDX\_CITAS\_MEDICO\_HORARIO (id\_medico, fecha\_hora\_inicio, fecha\_hora\_fin)`.

\- \*\*`TBL\_AGENDAS\_MEDICOS`\*\*: `ID\_AGENDA NUMBER(8)`, `ID\_MEDICO NUMBER(6)`, `ID\_ESTADO NUMBER(6)`, `FECHA\_HORA\_INICIO TIMESTAMP(6)`, `FECHA\_HORA\_FIN TIMESTAMP(6)`.

\- \*\*`TBL\_MEDICOS`\*\*: `ID\_MEDICO`, `ID\_USUARIO`, `ID\_ESPECIALIDAD`, `TARJETA\_PROFESIONAL`, `NOMBRES`, `APELLIDOS`, `TELEFONO`, `CORREO`.

\- \*\*`TBL\_USUARIOS`\*\*: `ID\_USUARIO`, `ID\_ENTIDAD`, `ID\_ESTADO`, `USUARIO`, `CONTRASENA`, `FECHA\_CREACION`.

\- \*\*Otras tablas en el esquema:\*\* `TBL\_PACIENTES`, `TBL\_ESPECIALIDADES`, `TBL\_FARMACEUTICOS`, `TBL\_HISTORIAS\_CLINICAS`, `TBL\_INCAPACIDADES`, `TBL\_RECETAS\_MEDICAS`, `TBL\_DETALLES\_RECETAS`, `TBL\_MEDICAMENTOS`, `TBL\_TIPOS\_MEDICAMENTOS`, `TBL\_STOCK\_FARMACIA`, `LOG\_AUDITORIA`.



\*\*Volumen de datos actual:\*\* 641 citas registradas (`14/09/2026` al `22/12/2026`) entre 15 médicos: 149 Programadas, 116 Confirmadas, 192 Atendidas, \*\*141 Canceladas (22.00%)\*\* y 43 No asistió. (No existen secuencias Oracle; los paquetes calculan `NVL(MAX(id), 0) + 1`).



\---



\## 4. Contrato de Paquetes PL/SQL, Triggers y VPD



Todos los llamados con `SimpleJdbcCall` deben usar `.withSchemaName("SISTEMACITASMEDICAS")` y `.withoutProcedureColumnMetaDataAccess()` declarando explícitamente cada `SqlParameter` y `SqlOutParameter`.



\### 4.1. `PKG\_CITAS` (HU01, HU02, HU09 + Bloques de 1 hora)

1\. \*\*`PRC\_RESERVAR\_CITA`\*\*

&#x20;  - Parámetros: `P\_ID\_PACIENTE (IN NUMBER)`, `P\_ID\_MEDICO (IN NUMBER)`, `P\_FECHA\_HORA\_INICIO (IN TIMESTAMP)`, `P\_FECHA\_HORA\_FIN (IN TIMESTAMP DEFAULT NULL)` \*(si es NULL asume 1 hora)\*.

&#x20;  - Comportamiento: Bloqueo pesimista `FOR UPDATE WAIT 3` sobre `TBL\_MEDICOS`, valida bloque activo (`401`/`402`) en `TBL\_AGENDAS\_MEDICOS` y ausencia de solapamientos en `TBL\_CITAS` (excluyendo `504`/`505`).

2\. \*\*`PRC\_CANCELAR\_CITA`\*\*

&#x20;  - Parámetros: `P\_ID\_CITA (IN NUMBER)`.

&#x20;  - Comportamiento: Pasa la cita a `504` y devuelve el bloque de agenda de `402` a `401` si aplica.

3\. \*\*`PRC\_CANCELACION\_MASIVA`\*\*

&#x20;  - Parámetros: `P\_ID\_MEDICO (IN NUMBER)`, `P\_FECHA (IN DATE)`.

&#x20;  - Comportamiento: `BULK COLLECT` + `FORALL` sobre citas (`501`/`502` -> `504`) y agendas (`401`/`402` -> `403`), registrando trazabilidad en `LOG\_AUDITORIA`.

4\. \*\*`PRC\_CALCULAR\_CUPOS\_BLOQUE`\*\*

&#x20;  - Parámetros: `P\_ID\_AGENDA (IN NUMBER)`, `P\_DURACION\_MIN (IN NUMBER DEFAULT 60)`, `P\_TOTAL\_CUPOS (OUT NUMBER)`, `P\_CUPOS\_DISPONIBLES (OUT NUMBER)`.

5\. \*\*`PRC\_OBTENER\_HORARIOS\_DISPONIBLES`\*\* \*(Alimenta el desplegable `<select>` del Frontend)\*

&#x20;  - Parámetros: `P\_ID\_MEDICO (IN NUMBER)`, `P\_FECHA (IN DATE)`, `P\_DURACION\_MIN (IN NUMBER DEFAULT 60)`, `P\_CURSOR (OUT SYS\_REFCURSOR)`.

&#x20;  - Columnas del cursor: `ID\_AGENDA`, `FECHA\_HORA\_INICIO`, `FECHA\_HORA\_FIN`, `ETIQUETA\_DESPLEGABLE` (formato `HH24:MI - HH24:MI`).



\### 4.2. `PKG\_REPORTES` (HU10)

1\. \*\*`PRC\_REPORTE\_MEDICOS\_CURSOR`\*\* \*(Consumo principal para el Dashboard Frontend)\*

&#x20;  - Parámetros: `P\_FECHA\_INICIO (IN DATE)`, `P\_FECHA\_FIN (IN DATE)`, `P\_CURSOR (OUT SYS\_REFCURSOR)`.

&#x20;  - Columnas del cursor: `ID\_MEDICO`, `NOMBRE\_MEDICO`, `TOTAL\_CITAS`, `PROGRAMADAS`, `CONFIRMADAS`, `ATENDIDAS`, `CANCELADAS`, `NO\_ASISTIO`, `PORCENTAJE\_CANCELACION`.

2\. \*\*`PRC\_RESUMEN\_ACTIVIDAD`\*\* (`P\_FECHA\_INICIO IN DATE`, `P\_FECHA\_FIN IN DATE`): salida por `DBMS\_OUTPUT` usando cursores explícitos.



\### 4.3. `PKG\_HISTORIA` (HU03, HU04) y `PKG\_FARMACIA` (HU05, HU06)

\- `PKG\_HISTORIA.PRC\_REGISTRAR\_CONSULTA`

\- `PKG\_FARMACIA.PRC\_DISPENSAR\_MEDICAMENTO`

\- \*Nota para el agente:\* Antes de implementar los repositorios de `historia` y `farmacia`, verificar las firmas exactas de estos dos procedimientos en `database/` o consultando `USER\_ARGUMENTS` en Oracle.



\### 4.4. Triggers Activos

\- `TRG\_VALIDAR\_CITA\_HISTORIA`, `TRG\_AUDIT\_HISTORIA`, `TRG\_ALERTA\_STOCK`.



\### 4.5. Seguridad VPD (HU07) a través de Pool de Conexiones (`CLIENT\_IDENTIFIER`)

Actualmente `FN\_FILTRO\_HISTORIAS\_MEDICO` filtra por `SYS\_CONTEXT('USERENV', 'SESSION\_USER')` y devuelve `NULL` (sin filtro) cuando el usuario conectado es `SISTEMACITASMEDICAS`.



\*\*Requerimiento pendiente a implementar en Backend + SQL:\*\*

Como Spring Boot usa un pool HikariCP conectado como `SISTEMACITASMEDICAS`, se debe habilitar el patrón estándar \*\*`CLIENT\_IDENTIFIER`\*\*:

1\. \*\*Ajuste en `FN\_FILTRO\_HISTORIAS\_MEDICO` (script SQL en `database/`):\*\*

&#x20;  - Leer `v\_client\_id := UPPER(SYS\_CONTEXT('USERENV', 'CLIENT\_IDENTIFIER'))`.

&#x20;  - Si `v\_client\_id IS NOT NULL`, filtrar `TBL\_HISTORIAS\_CLINICAS` usando `UPPER(u.usuario) = v\_client\_id` (salvo si es rol Administrador).

&#x20;  - Si `v\_client\_id IS NULL` y `SESSION\_USER = 'SISTEMACITASMEDICAS'`, mantener `RETURN NULL`.

2\. \*\*En Spring Boot (`HistoriaRepository`):\*\*

&#x20;  - Ejecutar sobre la \*\*misma `Connection`\*\* (`jdbcTemplate.execute((ConnectionCallback<...>) conn -> { ... })`):

&#x20;    1. `DBMS\_SESSION.SET\_IDENTIFIER(?)` con el username del médico autenticado.

&#x20;    2. Ejecutar el `SELECT` sobre `TBL\_HISTORIAS\_CLINICAS`.

&#x20;    3. En un bloque `finally` obligatorio, ejecutar `DBMS\_SESSION.CLEAR\_IDENTIFIER` antes de devolver la conexión al pool HikariCP.



\---



\## 5. Manejo Global de Errores Oracle (`GlobalExceptionHandler`)



Interceptar `UncategorizedSQLException` / `DataAccessException`, extraer `SQLException.getErrorCode()` y limpiar la primera línea del mensaje (removiendo `ORA-XXXXX:` y el stack `ORA-06512`) devolviendo JSON `{ "codigoOracle": 20001, "mensaje": "..." }`:



| Código Oracle | Significado de negocio | HTTP Status |

| :--- | :--- | :--- |

| `20001` | Médico ocupado / solapamiento / lock timeout (`FOR UPDATE WAIT 3`) | `409 Conflict` |

| `20002` | Horario fuera de los bloques de agenda del médico | `400 Bad Request` |

| `20003`, `20007`, `20009` | Médico, cita o bloque de agenda no encontrado | `404 Not Found` |

| `20004`, `20005`, `20006`, `20008`, `20010`, `20011` | Reglas de negocio (rango inválido, cita ya cancelada/atendida, fechas inválidas) | `400 Bad Request` |



\---



\## 6. Estructura de Carpetas Esperada



```text

sistema-citas-medicas/

├── README.md

├── database/                         # Scripts SQL finales (DDL, DML, PL/SQL, VPD + ajuste CLIENT\_IDENTIFIER)

├── backend/                          # Spring Boot 3 (Group: com.clinica, Artifact: citas-backend)

│   └── src/main/java/com/clinica/citas/

│       ├── config/                   # CorsConfig, GlobalExceptionHandler

│       ├── parametros/               # GET /api/parametros/{nombre}

│       ├── medicos/                  # GET /api/medicos (listado para selectores)

│       ├── citas/                    # Controller, Repository, DTOs (horarios, cupos, reservar, cancelar, masiva)

│       ├── historia/                 # Controller, Repository con CLIENT\_IDENTIFIER (HU03, HU04, HU07)

│       ├── farmacia/                 # Controller, Repository (HU05, HU06)

│       └── reportes/                 # Controller, Repository (HU10 -> SYS\_REFCURSOR)

└── frontend/                         # Vue 3 + Vite + Tailwind CSS + Axios

&#x20;   └── src/

&#x20;       ├── services/api.js           # Instancia Axios apuntando a http://localhost:8080/api

&#x20;       ├── components/               # Navbar, AlertBanner, HorariosSelect

&#x20;       └── views/                    # AgendarCitaView, GestionCitasView, HistoriaClinicaView, FarmaciaView, ReportesView

```



\---



\## 7. Endpoints REST Prioritarios (Fase Inicial)



1\. `GET /api/parametros/{nombre}` -> Consulta `TBL\_PARAMETROS` por categoría (`ESTADO\_CITA`, `GENERO`, etc.).

2\. `GET /api/medicos` -> Lista médicos activos (`ID\_MEDICO`, nombre completo, especialidad).

3\. `GET /api/citas/horarios-disponibles?idMedico=\&fecha=\&duracionMin=60` -> Llama `PKG\_CITAS.PRC\_OBTENER\_HORARIOS\_DISPONIBLES`.

4\. `GET /api/citas/cupos/{idAgenda}?duracionMin=60` -> Llama `PKG\_CITAS.PRC\_CALCULAR\_CUPOS\_BLOQUE`.

5\. `POST /api/citas/reservar` -> Llama `PKG\_CITAS.PRC\_RESERVAR\_CITA`.

6\. `PUT /api/citas/{idCita}/cancelar` -> Llama `PKG\_CITAS.PRC\_CANCELAR\_CITA`.

7\. `POST /api/citas/cancelacion-masiva` -> Llama `PKG\_CITAS.PRC\_CANCELACION\_MASIVA`.

8\. `GET /api/historias` (Header `X-Medico-Usuario`) -> Aplica `CLIENT\_IDENTIFIER` y consulta `TBL\_HISTORIAS\_CLINICAS` filtrada por VPD.

9\. `GET /api/reportes/medicos?fechaInicio=\&fechaFin=` -> Llama `PKG\_REPORTES.PRC\_REPORTE\_MEDICOS\_CURSOR`.



\---



\## 8. Configuración Local de Credenciales

En `backend/src/main/resources/application.properties` usar variables de entorno con defaults locales (nunca subir contraseñas reales a Git):

```properties

server.port=8080

spring.datasource.url=${ORACLE\_URL:jdbc:oracle:thin:@//localhost:1521/XEPDB1}

spring.datasource.username=${ORACLE\_USER:SISTEMACITASMEDICAS}

spring.datasource.password=${ORACLE\_PASS:CAMBIAR\_CLAVE}

spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.datasource.hikari.maximum-pool-size=10

```

