ALTER TABLE pacientes
ADD COLUMN email VARCHAR(255);

-- Criar um índice para a coluna email
CREATE INDEX idx_pacientes_email ON pacientes(email); 