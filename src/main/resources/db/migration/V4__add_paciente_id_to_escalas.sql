ALTER TABLE escalas ADD COLUMN paciente_id BIGINT;
ALTER TABLE escalas ADD CONSTRAINT fk_escalas_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id); 