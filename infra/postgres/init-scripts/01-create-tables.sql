-- 1. TABLA: EMPRESA (HU01)
CREATE TABLE empresa
(
    id_empresa           BIGSERIAL PRIMARY KEY,
    nombre               VARCHAR(150)       NOT NULL,
    ruc                  VARCHAR(20) UNIQUE NOT NULL,
    estado               BOOLEAN   DEFAULT TRUE,
    fecha_registro       TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Auditoría base
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLA: LOCAL (HU02)
CREATE TABLE local
(
    id_local               BIGSERIAL PRIMARY KEY,
    nombre                 VARCHAR(150) NOT NULL,
    direccion              VARCHAR(200),
    ciudad                 VARCHAR(100),
    estado                 BOOLEAN DEFAULT TRUE,
    id_empresa             BIGINT       NOT NULL,
    -- Mejora: Campo para controlar el cooldown de 6 horas de alertas [cite: 266]
    ultima_alerta_generada TIMESTAMP,
    CONSTRAINT fk_local_empresa
        FOREIGN KEY (id_empresa)
            REFERENCES empresa (id_empresa)
            ON DELETE CASCADE
);

-- 3. TABLA: OBJETIVO_VENTA (HU03)
CREATE TABLE objetivo_venta
(
    id_objetivo    BIGSERIAL PRIMARY KEY,
    id_local       BIGINT         NOT NULL,
    monto_objetivo DECIMAL(12, 2) NOT NULL CHECK (monto_objetivo > 0),
    tipo_periodo   VARCHAR(10) CHECK (tipo_periodo IN ('SEMANAL', 'MENSUAL')), --
    fecha_inicio   DATE           NOT NULL,
    fecha_fin      DATE           NOT NULL,
    estado         VARCHAR(15) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'FINALIZADO')),
    -- Mejora: Validación de coherencia de fechas
    CONSTRAINT chk_fechas_logicas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT fk_objetivo_local
        FOREIGN KEY (id_local)
            REFERENCES local (id_local)
            ON DELETE CASCADE
);

-- 4. TABLA: VENTA (HU04)
CREATE TABLE venta
(
    id_venta               BIGSERIAL PRIMARY KEY,
    id_local               BIGINT         NOT NULL,
    fecha_venta            DATE           NOT NULL,
    monto                  DECIMAL(12, 2) NOT NULL CHECK (monto >= 0),
    descripcion            VARCHAR(200),
    fecha_registro_sistema TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Trazabilidad
    CONSTRAINT fk_venta_local
        FOREIGN KEY (id_local)
            REFERENCES local (id_local)
            ON DELETE CASCADE
);

-- 5. TABLA: NOTIFICACION (HU06)
CREATE TABLE notificacion
(
    id_notificacion BIGSERIAL PRIMARY KEY,
    id_local        BIGINT NOT NULL,
    tipo            VARCHAR(20) CHECK (tipo IN ('ALERTA', 'AVANCE', 'SIN_VENTAS')),        --
    nivel_gravedad  VARCHAR(15) CHECK (nivel_gravedad IN ('WARNING', 'CRITICAL', 'INFO')), -- [cite: 205]
    mensaje         VARCHAR(255),
    fecha_generada  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    leida           BOOLEAN   DEFAULT FALSE,
    CONSTRAINT fk_notificacion_local
        FOREIGN KEY (id_local)
            REFERENCES local (id_local)
            ON DELETE CASCADE
);

-- 6. TABLA: USUARIO (HU09)
CREATE TABLE usuario
(
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(100)        NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,                       -- Espacio para Hash BCrypt
    rol        VARCHAR(20) CHECK (rol IN ('ADMIN', 'SUPERVISOR', 'GERENTE')), --
    id_empresa BIGINT              NOT NULL,
    CONSTRAINT fk_usuario_empresa
        FOREIGN KEY (id_empresa)
            REFERENCES empresa (id_empresa)
            ON DELETE CASCADE
);
