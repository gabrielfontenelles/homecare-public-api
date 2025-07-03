-- Inserir alguns pacientes de exemplo
INSERT INTO pacientes (nome, data_nascimento, telefone, endereco, cidade, estado, status, created_at)
VALUES
('Roberto Mendes', '1953-05-15', '(11) 98765-4321', 'Rua das Flores, 123', 'São Paulo', 'SP', 'ATIVO', CURRENT_TIMESTAMP),
('Maria Silva', '1958-08-22', '(11) 91234-5678', 'Av. Paulista, 1000', 'São Paulo', 'SP', 'ATIVO', CURRENT_TIMESTAMP),
('João Costa', '1945-03-10', '(11) 99876-5432', 'Rua Augusta, 500', 'São Paulo', 'SP', 'ATIVO', CURRENT_TIMESTAMP),
('Ana Paula', '1957-11-30', '(11) 95555-4444', 'Rua Consolação, 200', 'São Paulo', 'SP', 'PENDENTE', CURRENT_TIMESTAMP),
('Carlos Santos', '1950-07-18', '(11) 94444-3333', 'Av. Rebouças, 300', 'São Paulo', 'SP', 'ATIVO', CURRENT_TIMESTAMP);

-- Inserir alguns profissionais de exemplo
INSERT INTO profissionais (nome, email, telefone, especialidade, registro_profissional, status, created_at)
VALUES
('Dra. Carla Mendes', 'carla.mendes@homecarecoop.com', '(11) 98765-4321', 'Fisioterapeuta', 'CREFITO-123456', 'DISPONIVEL', CURRENT_TIMESTAMP),
('Enf. Ricardo Santos', 'ricardo.santos@homecarecoop.com', '(11) 97654-3210', 'Enfermeiro', 'COREN-654321', 'EM_ATENDIMENTO', CURRENT_TIMESTAMP),
('Dra. Patricia Lima', 'patricia.lima@homecarecoop.com', '(11) 96543-2109', 'Nutricionista', 'CRN-987654', 'DISPONIVEL', CURRENT_TIMESTAMP),
('Dr. André Oliveira', 'andre.oliveira@homecarecoop.com', '(11) 95432-1098', 'Médico Geriatra', 'CRM-456789', 'FOLGA', CURRENT_TIMESTAMP),
('Téc. Ana Beatriz', 'ana.beatriz@homecarecoop.com', '(11) 94321-0987', 'Técnica de Enfermagem', 'COREN-123789', 'EM_ATENDIMENTO', CURRENT_TIMESTAMP);

-- Inserir algumas escalas de exemplo
INSERT INTO escalas (profissional_id, data, hora_inicio, hora_fim, status, created_at)
VALUES
(1, CURRENT_DATE, '08:00', '12:00', 'COMPLETA', CURRENT_TIMESTAMP),
(1, CURRENT_DATE, '14:00', '18:00', 'COMPLETA', CURRENT_TIMESTAMP),
(2, CURRENT_DATE, '07:00', '13:00', 'COMPLETA', CURRENT_TIMESTAMP),
(3, CURRENT_DATE, '09:00', '15:00', 'COMPLETA', CURRENT_TIMESTAMP),
(5, CURRENT_DATE, '08:00', '14:00', 'PARCIAL', CURRENT_TIMESTAMP);

-- Inserir alguns agendamentos de exemplo
INSERT INTO agendamentos (paciente_id, profissional_id, data, hora_inicio, hora_fim, tipo, status, endereco_atendimento, created_at)
VALUES
(1, 1, CURRENT_DATE, '09:00', '10:00', 'Fisioterapia', 'CONFIRMADO', 'Rua das Flores, 123 - São Paulo, SP', CURRENT_TIMESTAMP),
(2, 2, CURRENT_DATE, '11:30', '12:30', 'Enfermagem', 'CONFIRMADO', 'Av. Paulista, 1000 - São Paulo, SP', CURRENT_TIMESTAMP),
(3, 3, CURRENT_DATE, '14:00', '15:00', 'Nutrição', 'PENDENTE', 'Rua Augusta, 500 - São Paulo, SP', CURRENT_TIMESTAMP);

