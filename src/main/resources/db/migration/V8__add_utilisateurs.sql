-- Table utilisateurs pour l'authentification de la secretaire.
-- Le user par defaut (username=secretaire, password=secretaire2026) est seede
-- au demarrage par DefaultUserSeeder si la table est vide.
CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
