ALTER TABLE agendamentos ADD COLUMN modalidade VARCHAR(20) NOT NULL DEFAULT 'IDA';
ALTER TABLE agendamentos ADD COLUMN especialidade VARCHAR(100);
ALTER TABLE agendamentos ALTER COLUMN profissional_id DROP NOT NULL; 