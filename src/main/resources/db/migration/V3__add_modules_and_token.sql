-- Ajouter response_token si absent
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='professeurs' AND column_name='response_token') THEN
        ALTER TABLE professeurs ADD COLUMN response_token VARCHAR(64);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='professeurs' AND column_name='token_expire_at') THEN
        ALTER TABLE professeurs ADD COLUMN token_expire_at TIMESTAMP;
    END IF;
END $$;

-- Créer table modules
CREATE TABLE IF NOT EXISTS modules (
    id             BIGSERIAL    PRIMARY KEY,
    nom            VARCHAR(150) NOT NULL,
    niveau         VARCHAR(10)  NOT NULL CHECK (niveau IN ('L1','L2','L3','M1','M2')),
    nombre_heures  INTEGER      NOT NULL DEFAULT 0,
    professeur_id  BIGINT       REFERENCES professeurs(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_modules_professeur ON modules(professeur_id);
CREATE INDEX IF NOT EXISTS idx_profs_token ON professeurs(response_token);

-- Données demo modules
INSERT INTO modules (nom, niveau, nombre_heures, professeur_id) VALUES
  ('Développement Web', 'L3', 60, 1),
  ('Base de données', 'L2', 45, 2),
  ('Algorithmes', 'L1', 40, 3)
ON CONFLICT DO NOTHING;
