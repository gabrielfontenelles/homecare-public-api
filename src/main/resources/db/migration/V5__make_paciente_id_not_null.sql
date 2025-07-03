-- Primeiro, vamos garantir que não há registros nulos
UPDATE escalas SET paciente_id = (SELECT id FROM pacientes LIMIT 1) WHERE paciente_id IS NULL;

-- Agora podemos tornar a coluna NOT NULL
ALTER TABLE escalas ALTER COLUMN paciente_id SET NOT NULL; 