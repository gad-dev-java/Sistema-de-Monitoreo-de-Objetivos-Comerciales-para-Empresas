INSERT INTO empresa (nombre, ruc)
VALUES ('Corporación Textil S.A.', '20123456789'),
       ('Inversiones Alimenticias S.A.C.', '20987654321'),
       ('Tecnología Global Perú', '20556677889'),
       ('Distribuidora del Sur', '20443322110'),
       ('Retail Innova E.I.R.L.', '20667788991');

INSERT INTO local (nombre, direccion, ciudad, id_empresa)
VALUES ('Sede Central Lima', 'Av. Arequipa 1234', 'Lima', 1),
       ('Sucursal San Isidro', 'Calle Las Orquídeas 567', 'Lima', 1),
       ('Planta Industrial Lurín', 'Km 25 Panamericana Sur', 'Lurín', 2),
       ('Tienda Express Arequipa', 'Calle Mercaderes 101', 'Arequipa', 3),
       ('Almacén Central Trujillo', 'Av. Larco 890', 'Trujillo', 4);

INSERT INTO objetivo_venta (id_local, monto_objetivo, tipo_periodo, fecha_inicio, fecha_fin, estado)
VALUES (1, 50000.00, 'MENSUAL', '2026-03-01', '2026-03-31', 'ACTIVO'),
       (2, 15000.00, 'SEMANAL', '2026-03-01', '2026-03-07', 'ACTIVO'),
       (3, 80000.00, 'MENSUAL', '2026-03-01', '2026-03-31', 'ACTIVO'),
       (4, 20000.00, 'MENSUAL', '2026-03-01', '2026-03-31', 'ACTIVO'),
       (5, 12000.00, 'SEMANAL', '2026-03-01', '2026-03-07', 'ACTIVO');

INSERT INTO venta (id_local, fecha_venta, monto, descripcion)
VALUES (1, '2026-03-02', 2500.50, 'Venta de temporada mañana'),
       (1, '2026-03-03', 1800.00, 'Venta corporativa tarde'),
       (2, '2026-03-02', 950.00, 'Venta minorista'),
       (3, '2026-03-04', 12400.00, 'Despacho mayorista Lote A'),
       (4, '2026-03-05', 3100.20, 'Venta directa tienda');

INSERT INTO notificacion (id_local, tipo, nivel_gravedad, mensaje)
VALUES (1, 'AVANCE', 'INFO', 'El local ha alcanzado el 50% de su meta mensual.'),
       (2, 'ALERTA', 'WARNING', 'Ventas por debajo del promedio esperado para la semana.'),
       (5, 'SIN_VENTAS', 'CRITICAL', 'No se registran ventas en las últimas 48 horas.'),
       (3, 'AVANCE', 'INFO', 'Meta mensual completada satisfactoriamente.'),
       (4, 'ALERTA', 'CRITICAL', 'Desviación crítica del objetivo: PC menor al 20%.');

INSERT INTO usuario (nombre, email, password, rol, id_empresa)
VALUES ('Carlos Pérez', 'cperez@empresa1.com', '$2a$10$r.82mY..hash_ejemplo_1', 'ADMIN', 1),
       ('Ana García', 'agarcia@empresa1.com', '$2a$10$r.82mY..hash_ejemplo_2', 'SUPERVISOR', 1),
       ('Roberto Gómez', 'rgomez@empresa2.com', '$2a$10$r.82mY..hash_ejemplo_3', 'ADMIN', 2),
       ('Lucía Torres', 'ltorres@empresa3.com', '$2a$10$r.82mY..hash_ejemplo_4', 'SUPERVISOR', 3),
       ('Miguel Jara', 'mjara@empresa4.com', '$2a$10$r.82mY..hash_ejemplo_5', 'ADMIN', 4);