-- Execute como usuário administrador do PostgreSQL.
-- Troque a senha antes de rodar em um ambiente real.

CREATE USER coopmanager WITH PASSWORD 'troque_esta_senha';
CREATE DATABASE coopmanager OWNER coopmanager;

-- O CoopManager cria as tabelas automaticamente na primeira conexão.
