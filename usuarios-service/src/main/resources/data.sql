-- 1. Crear los roles por defecto (Asumiendo que 1 es 'activo')
INSERT IGNORE INTO rol (id_rol, activo, name) VALUES (1, 1, 'ADMIN');
INSERT IGNORE INTO rol (id_rol, activo, name) VALUES (2, 1, 'ALUMNO');
INSERT IGNORE INTO rol (id_rol, activo, name) VALUES (3, 1, 'PROFESOR');

-- 2. Crear el usuario Administrador
-- La contraseña insertada es 'admin' (ya encriptada en BCrypt para que funcione el login)
INSERT IGNORE INTO usuarios (id_user, activo, nombre_usuario, apellido_usuario, correo_usuario, contrasenha_usuario)
VALUES (1, 1, 'Jefe', 'Estudios', 'admin@gmail.com', '$2a$10$fKi12tHGj5lTBfgZ38.arelgjrIOseK241JA4FPCm6K7D4qLe.BCq');

-- 3. Vincular el Administrador con el rol ADMIN
INSERT IGNORE INTO roles_usuario (id_user, id_rol) VALUES (1, 1);