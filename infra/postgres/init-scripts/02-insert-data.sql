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
VALUES (1, 50000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'FINALIZADO'),
       (1, 12000.00, 'SEMANAL', '2024-03-04', '2024-03-10', 'ACTIVO'),
       (2, 30000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (3, 15000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (4, 80000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (4, 20000.00, 'SEMANAL', '2024-03-04', '2024-03-10', 'FINALIZADO'),
       (5, 45000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (6, 100000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (7, 60000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (8, 25000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (9, 40000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (10, 90000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (11, 35000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (12, 20000.00, 'MENSUAL', '2024-03-01', '2024-03-31', 'ACTIVO'),
       (1, 45000.00, 'MENSUAL', '2024-02-01', '2024-02-29', 'FINALIZADO'),
       (4, 75000.00, 'MENSUAL', '2024-02-01', '2024-02-29', 'FINALIZADO'),
       (7, 55000.00, 'MENSUAL', '2024-02-01', '2024-02-29', 'FINALIZADO'),
       (10, 85000.00, 'MENSUAL', '2024-02-01', '2024-02-29', 'FINALIZADO'),
       (2, 7000.00, 'SEMANAL', '2024-03-04', '2024-03-10', 'FINALIZADO'),
       (12, 5000.00, 'SEMANAL', '2024-03-04', '2024-03-10', 'FINALIZADO');

INSERT INTO venta (id_local, fecha_venta, monto, descripcion)
VALUES (1, '2024-03-05', 1500.50, 'Venta corporativa de telas'),
       (1, '2024-03-06', 800.00, 'Venta al detalle minorista'),
       (4, '2024-03-05', 3500.00, 'Catering evento empresarial'),
       (4, '2024-03-06', 2100.20, 'Ventas del día salón'),
       (7, '2024-03-05', 12000.00, 'Venta de 5 Laptops Core i7'),
       (10, '2024-03-05', 450.00, 'Servicio de despacho local'),
       (2, '2024-03-06', 950.00, 'Venta de uniformes'),
       (5, '2024-03-06', 1200.00, 'Reserva de mesa 10 personas'),
       (8, '2024-03-05', 300.00, 'Mantenimiento preventivo PC'),
       (11, '2024-03-05', 5600.00, 'Envío regional carga pesada'),
       (12, '2024-03-06', 150.00, 'Venta rápida mostrador'),
       (3, '2024-03-06', 4000.00, 'Lote de hilos industriales'),
       (6, '2024-03-05', 8900.50, 'Producción lote semanal'),
       (9, '2024-03-06', 2500.00, 'Venta accesorios tech'),
       (1, '2024-03-07', 2200.00, 'Venta de temporada'),
       (4, '2024-03-07', 1800.00, 'Consumo restaurante'),
       (7, '2024-03-07', 500.00, 'Licencia de software'),
       (10, '2024-03-07', 3200.00, 'Almacenaje de mercadería'),
       (2, '2024-03-07', 450.00, 'Accesorios de costura'),
       (5, '2024-03-07', 900.00, 'Almuerzo ejecutivo');


INSERT INTO notificacion (id_local, tipo, nivel_gravedad, mensaje)
VALUES (1, 'ALERTA', 'CRITICAL', 'Venta diaria por debajo del 20% del objetivo'),
       (4, 'AVANCE', 'INFO', 'Has alcanzado el 50% de tu objetivo mensual'),
       (10, 'SIN_VENTAS', 'WARNING', 'No se han registrado ventas en las últimas 24 horas'),
       (7, 'ALERTA', 'CRITICAL', 'Posible caída de ingresos respecto a la semana pasada'),
       (2, 'AVANCE', 'INFO', 'Buen ritmo de ventas en la sucursal Trujillo'),
       (12, 'SIN_VENTAS', 'WARNING', 'Alerta: Local Cusco sin actividad registrada'),
       (1, 'AVANCE', 'INFO', 'Objetivo semanal completado con éxito'),
       (5, 'ALERTA', 'WARNING', 'Ligero retraso en el cumplimiento de cuota'),
       (8, 'INFO', 'INFO', 'Actualización de sistema completada en Arequipa'),
       (11, 'AVANCE', 'INFO', 'Meta del 75% alcanzada'),
       (3, 'SIN_VENTAS', 'CRITICAL', 'Urgente: Almacén Chiclayo sin reportes de salida'),
       (6, 'ALERTA', 'WARNING', 'Revisar margen de ganancia en Planta Ate'),
       (9, 'AVANCE', 'INFO', 'Venta destacada en Showroom Surco'),
       (4, 'SIN_VENTAS', 'WARNING', 'Revisar cierre de caja Restaurante Miraflores'),
       (7, 'AVANCE', 'INFO', 'Nuevo récord de ventas en San Miguel'),
       (1, 'ALERTA', 'CRITICAL', 'Falta de registro de metas para el próximo periodo'),
       (10, 'AVANCE', 'INFO', 'Operación Callao fluyendo según lo planeado'),
       (2, 'SIN_VENTAS', 'WARNING', 'Verificar conexión de red en sucursal Trujillo'),
       (12, 'AVANCE', 'INFO', 'Incremento del 10% en ventas locales'),
       (5, 'ALERTA', 'CRITICAL', 'Meta mensual en riesgo en San Isidro');
-- INSERT INTO empresa (nombre, ruc)
-- VALUES ('Corporación Textil S.A.', '20123456789'),
--        ('Inversiones Alimenticias S.A.C.', '20987654321'),
--        ('Tecnología Global Perú', '20556677889'),
--        ('Distribuidora del Sur', '20443322110'),
--        ('Retail Innova E.I.R.L.', '20667788991');
--
-- INSERT INTO local (nombre, direccion, ciudad, id_empresa)
-- VALUES ('Sede Central Lima', 'Av. Arequipa 1234', 'Lima', 1),
--        ('Sucursal San Isidro', 'Calle Las Orquídeas 567', 'Lima', 1),
--        ('Planta Industrial Lurín', 'Km 25 Panamericana Sur', 'Lurín', 2),
--        ('Tienda Express Arequipa', 'Calle Mercaderes 101', 'Arequipa', 3),
--        ('Almacén Central Trujillo', 'Av. Larco 890', 'Trujillo', 4);
--
-- INSERT INTO objetivo_venta (id_local, monto_objetivo, tipo_periodo, fecha_inicio, fecha_fin, estado)
-- VALUES (1, 50000.00, 'MENSUAL', '2026-03-01', '2026-03-31', 'ACTIVO'),
--        (2, 15000.00, 'SEMANAL', '2026-03-01', '2026-03-07', 'ACTIVO'),
--        (3, 80000.00, 'MENSUAL', '2026-03-01', '2026-03-31', 'ACTIVO'),
--        (4, 20000.00, 'MENSUAL', '2026-03-01', '2026-03-31', 'ACTIVO'),
--        (5, 12000.00, 'SEMANAL', '2026-03-01', '2026-03-07', 'ACTIVO');
--
-- INSERT INTO venta (id_local, fecha_venta, monto, descripcion)
-- VALUES (1, '2026-03-02', 2500.50, 'Venta de temporada mañana'),
--        (1, '2026-03-03', 1800.00, 'Venta corporativa tarde'),
--        (2, '2026-03-02', 950.00, 'Venta minorista'),
--        (3, '2026-03-04', 12400.00, 'Despacho mayorista Lote A'),
--        (4, '2026-03-05', 3100.20, 'Venta directa tienda');
--
-- INSERT INTO notificacion (id_local, tipo, nivel_gravedad, mensaje)
-- VALUES (1, 'AVANCE', 'INFO', 'El local ha alcanzado el 50% de su meta mensual.'),
--        (2, 'ALERTA', 'WARNING', 'Ventas por debajo del promedio esperado para la semana.'),
--        (5, 'SIN_VENTAS', 'CRITICAL', 'No se registran ventas en las últimas 48 horas.'),
--        (3, 'AVANCE', 'INFO', 'Meta mensual completada satisfactoriamente.'),
--        (4, 'ALERTA', 'CRITICAL', 'Desviación crítica del objetivo: PC menor al 20%.');
--
-- INSERT INTO usuario (nombre, email, password, rol, id_empresa)
-- VALUES ('Carlos Pérez', 'cperez@empresa1.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy', 'ADMIN', 1),
--        ('Ana García', 'agarcia@empresa1.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy', 'SUPERVISOR', 1),
--        ('Roberto Gómez', 'rgomez@empresa2.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy', 'ADMIN', 2),
--        ('Lucía Torres', 'ltorres@empresa3.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy', 'SUPERVISOR', 3),
--        ('Miguel Jara', 'mjara@empresa4.com', '$2a$10$K6cphovuyyeTUdeDKfMjteXaSKq7mQQ41RZnhKouvKfmwujsfzEqy', 'GERENTE', 4);