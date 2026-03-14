INSERT INTO empresa (nombre, ruc)
VALUES ('Corporación Textil del Norte', '20123456789'),
       ('Alimentos & Sabores S.A.', '20987654321'),
       ('Tecnología Global Perú', '20556677889'),
       ('Inversiones Logísticas Lima', '20334455667');

INSERT INTO local (nombre, direccion, ciudad, id_empresa)
VALUES ('Sede Central - Olivos', 'Av. Carlos Izaguirre 123', 'Lima', 1),
       ('Sucursal Trujillo', 'Jr. Pizarro 456', 'Trujillo', 1),
       ('Almacén Chiclayo', 'Av. Balta 789', 'Chiclayo', 1),
       ('Restaurante Miraflores', 'Av. Larco 1010', 'Lima', 2),
       ('Restaurante San Isidro', 'Av. Dos de Mayo 550', 'Lima', 2),
       ('Planta de Producción Ate', 'Av. Nicolás Ayllón 2200', 'Lima', 2),
       ('Tienda Tech San Miguel', 'Av. La Marina 2500', 'Lima', 3),
       ('Soporte Arequipa', 'Calle Mercaderes 300', 'Arequipa', 3),
       ('Showroom Surco', 'Av. Primavera 880', 'Lima', 3),
       ('Centro Logístico Callao', 'Av. Faucett 400', 'Callao', 4),
       ('Distribuidora Huancayo', 'Av. Giraldez 150', 'Huancayo', 4),
       ('Local Express Cusco', 'Av. El Sol 900', 'Cusco', 4);

INSERT INTO usuario (nombre, email, password, rol, id_empresa)
VALUES ('Carlos Mendoza', 'cmendoza@textilnorte.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy',
        'ADMIN', 1),
       ('Ana García', 'agarcia@alimentos.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy',
        'GERENTE', 2),
       ('Luis Torres', 'ltorres@techglobal.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy',
        'SUPERVISOR', 3),
       ('Elena Paz', 'epaz@logistica.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy', 'ADMIN', 4),
       ('Jorge Rivas', 'jrivas@textilnorte.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy',
        'SUPERVISOR', 1);

INSERT INTO objetivo_venta (id_local, monto_objetivo, tipo_periodo, fecha_inicio, fecha_fin, estado)
VALUES (1, 50000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (2, 30000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (3, 15000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (4, 20000.00, 'SEMANAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (5, 45000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (6, 100000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (7, 60000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (8, 25000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (9, 40000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (10, 90000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (11, 35000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO'),
       (12, 35000.00, 'MENSUAL', '2026-03-01', '2026-04-13', 'ACTIVO');

INSERT INTO venta (id_local, fecha_venta, monto, descripcion)
VALUES (1, '2026-03-13', 11000.00, 'Venta para validar estado WARNING');
