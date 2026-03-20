-- 1. Tabla para almacenar los exámenes
Use examenes;

CREATE TABLE IF NOT EXISTS examenes (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        titulo VARCHAR(255) NOT NULL,
    descripcion TEXT
    );

-- 2. Tabla para almacenar el banco global de preguntas
CREATE TABLE IF NOT EXISTS preguntas (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         enunciado TEXT NOT NULL,
                                         opciona VARCHAR(255) NOT NULL,
    opcionb VARCHAR(255) NOT NULL,
    opcionc VARCHAR(255) NOT NULL,
    opciond VARCHAR(255) NOT NULL,
    correcta VARCHAR(1) NOT NULL
    );

-- 3. Tabla intermedia (Relación de Muchos a Muchos)
-- Esta es la que Spring Data JPA buscaba automáticamente al hacer el @ManyToMany
CREATE TABLE IF NOT EXISTS examen_pregunta (
                                               examen_id BIGINT NOT NULL,
                                               pregunta_id BIGINT NOT NULL,
                                               PRIMARY KEY (examen_id, pregunta_id),
    CONSTRAINT fk_examen FOREIGN KEY (examen_id) REFERENCES examenes(id) ON DELETE CASCADE,
    CONSTRAINT fk_pregunta FOREIGN KEY (pregunta_id) REFERENCES preguntas(id) ON DELETE CASCADE
    );

-- 4. Tabla para registrar las notas de los alumnos (Intento/Evaluación)
CREATE TABLE IF NOT EXISTS evaluaciones (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            corre_usuario VARCHAR(200) NOT NULL,
                                            id_examen BIGINT NOT NULL,
                                            nota DOUBLE NOT NULL,
                                            fecha DATETIME NOT NULL
);

INSERT INTO preguntas (enunciado, opciona, opcionb, opcionc, opciond, correcta)
VALUES ('¿Qué piloto tiene el récord de más Campeonatos Mundiales de F1 (empatado con 7)?', 'Ayrton Senna',
        'Alain Prost', 'Michael Schumacher', 'Sebastian Vettel', 'C'),
       ('¿En qué año se celebró el primer Campeonato Mundial de Fórmula 1 oficial?', '1950', '1945', '1960', '1932',
        'A'),
       ('¿Quién es el piloto con más victorias en la historia de la F1?', 'Michael Schumacher', 'Lewis Hamilton',
        'Max Verstappen', 'Juan Manuel Fangio', 'B'),
       ('¿De qué color es tradicionalmente el monoplaza de Ferrari?', 'Azul', 'Rojo', 'Amarillo', 'Plata', 'B'),
       ('¿Qué significan las siglas DRS en la Fórmula 1?', 'Dynamic Racing Setup', 'Downforce Reduction System',
        'Drag Reduction System', 'Direct Racing Strategy', 'C'),
       ('¿En qué ciudad se corre el famoso Gran Premio de Mónaco?', 'Niza', 'Montecarlo', 'Cannes', 'Marsella', 'B'),
       ('¿Quién es el piloto más joven en ganar una carrera de Fórmula 1?', 'Max Verstappen', 'Sebastian Vettel',
        'Fernando Alonso', 'Charles Leclerc', 'A'),
       ('¿Cuántos campeonatos del mundo ha ganado el español Fernando Alonso?', '1', '2', '3', '4', 'B'),
       ('¿Qué escudería tiene el récord de más campeonatos de constructores?', 'McLaren', 'Mercedes', 'Ferrari',
        'Williams', 'C'),
       ('¿De qué nacionalidad era el legendario piloto Ayrton Senna?', 'Argentino', 'Italiano', 'Brasileño',
        'Portugués', 'C'),
       ('¿Qué bandera se utiliza para indicar el final de una carrera?', 'Bandera Roja', 'Bandera Verde',
        'Bandera a Cuadros', 'Bandera Negra', 'C'),
       ('¿Cuántos puntos se otorgan actualmente al ganador de una carrera (sin contar la vuelta rápida)?', '10', '15',
        '20', '25', 'D'),
       ('¿A qué piloto se le conoce con el apodo de "El Nano"?', 'Fernando Alonso', 'Carlos Sainz', 'Pedro de la Rosa',
        'Marc Gené', 'A'),
       ('¿A qué piloto se le apoda el "Honey Badger"?', 'Lando Norris', 'Daniel Ricciardo', 'Valtteri Bottas',
        'Kevin Magnussen', 'B'),
       ('¿En qué año ganó Lewis Hamilton su primer campeonato mundial?', '2007', '2008', '2014', '2015', 'B'),
       ('¿Dónde se encuentra el Circuito de las Américas (COTA)?', 'Miami', 'Las Vegas', 'Austin', 'Indianápolis', 'C'),
       ('¿Qué significa que los comisarios agiten una bandera amarilla?', 'Peligro en pista, reducir velocidad',
        'Coche lento delante', 'Lluvia inminente', 'Fin de la sesión', 'A'),
       ('¿Qué color tiene la franja del neumático compuesto Blando (Soft) de Pirelli?', 'Blanco', 'Amarillo', 'Rojo',
        'Azul', 'C'),
       ('¿Cuántos pilotos compiten oficialmente para cada equipo en una temporada normal?', '1', '2', '3', '4', 'B'),
       ('¿Qué piloto español corrió para Ferrari en la temporada 2024?', 'Fernando Alonso', 'Álex Palou',
        'Carlos Sainz', 'Jaime Alguersuari', 'C'),
       ('¿Quién ganó el polémico Gran Premio de Abu Dhabi en 2021?', 'Lewis Hamilton', 'Max Verstappen', 'Sergio Pérez',
        'Valtteri Bottas', 'B'),
       ('¿Cuál era el nombre anterior de la escudería Alpine?', 'Force India', 'Toro Rosso', 'Sauber', 'Renault', 'D'),
       ('¿Para qué sirve el elemento de seguridad llamado "Halo"?', 'Proteger la cabeza del piloto',
        'Mejorar la aerodinámica', 'Aumentar la visibilidad', 'Refrigerar el motor', 'A'),
       ('¿Con qué piloto británico tuvo Niki Lauda una famosa rivalidad en los años 70?', 'Nigel Mansell',
        'Jackie Stewart', 'James Hunt', 'Graham Hill', 'C'),
       ('¿Cómo se llama la penalización en la que el piloto pasa por boxes a velocidad reducida sin detenerse?',
        'Stop and Go', 'Drive Through', 'Time Penalty', 'Grid Drop', 'B'),
       ('¿Qué piloto es conocido como "El Profesor"?', 'Niki Lauda', 'Alain Prost', 'Nelson Piquet', 'Nigel Mansell',
        'B'),
       ('¿En qué circuito se disputa el Gran Premio de Gran Bretaña?', 'Brands Hatch', 'Donington Park', 'Silverstone',
        'Goodwood', 'C'),
       ('¿Quién ostenta el récord de más poles consecutivas en la historia de la F1?', 'Ayrton Senna', 'Lewis Hamilton',
        'Michael Schumacher', 'Sebastian Vettel', 'A'),
       ('¿Qué marca de neumáticos es el proveedor único oficial de la F1 desde 2011?', 'Michelin', 'Bridgestone',
        'Goodyear', 'Pirelli', 'D'),
       ('¿En qué país se celebra el primer Gran Premio de la temporada habitualmente en la era moderna?',
        'Australia / Bahréin', 'España', 'Japón', 'Mónaco', 'A');