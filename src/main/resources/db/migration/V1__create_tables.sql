CREATE TABLE IF NOT EXISTS professeurs (
    id              BIGSERIAL       PRIMARY KEY,
    nom             VARCHAR(150)    NOT NULL,
    matiere         VARCHAR(150)    NOT NULL,
    email           VARCHAR(200)    NOT NULL UNIQUE,
    telephone       VARCHAR(30),
    whatsapp_numero VARCHAR(30),
    whatsapp_statut VARCHAR(20)     NOT NULL DEFAULT 'ATTENTE'
                    CHECK (whatsapp_statut IN ('ATTENTE', 'ENVOYE', 'REPONDU')),
    CONSTRAINT uq_professeur_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS etudiants (
    id       BIGSERIAL    PRIMARY KEY,
    nom      VARCHAR(150) NOT NULL,
    email    VARCHAR(200) NOT NULL UNIQUE,
    filiere  VARCHAR(100) NOT NULL,
    niveau   VARCHAR(10)  NOT NULL CHECK (niveau IN ('L1','L2','L3','M1','M2')),
    CONSTRAINT uq_etudiant_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS disponibilites (
    id             BIGSERIAL    PRIMARY KEY,
    professeur_id  BIGINT       NOT NULL REFERENCES professeurs(id) ON DELETE CASCADE,
    filiere        VARCHAR(100) NOT NULL,
    niveau         VARCHAR(10)  NOT NULL,
    salle          VARCHAR(100),
    jour           DATE         NOT NULL,
    heure_debut    TIME         NOT NULL,
    heure_fin      TIME         NOT NULL,
    statut         VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE'
                   CHECK (statut IN ('DISPONIBLE', 'CONFIRME', 'ANNULE')),
    CONSTRAINT chk_heure_ordre CHECK (heure_fin > heure_debut)
);

CREATE INDEX IF NOT EXISTS idx_dispos_professeur ON disponibilites(professeur_id);
CREATE INDEX IF NOT EXISTS idx_dispos_filiere_niveau ON disponibilites(filiere, niveau);
CREATE INDEX IF NOT EXISTS idx_dispos_jour ON disponibilites(jour);

CREATE TABLE IF NOT EXISTS emplois_du_temps (
    id            BIGSERIAL     PRIMARY KEY,
    filiere       VARCHAR(100)  NOT NULL,
    niveau        VARCHAR(10)   NOT NULL,
    semaine_du    DATE          NOT NULL,
    semaine_au    DATE          NOT NULL,
    creneaux_json TEXT,
    statut        VARCHAR(20)   NOT NULL DEFAULT 'GENERE'
                  CHECK (statut IN ('GENERE', 'ENVOYE')),
    date_creation TIMESTAMP     NOT NULL DEFAULT NOW()
);
