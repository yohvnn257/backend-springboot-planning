-- V5 : Système de tickets support utilisateur
CREATE TABLE IF NOT EXISTS tickets (
    id              BIGSERIAL    PRIMARY KEY,
    titre           VARCHAR(200) NOT NULL,
    description     TEXT,
    type            VARCHAR(20)  NOT NULL DEFAULT 'BUG'
                    CHECK (type IN ('BUG','SUGGESTION','QUESTION')),
    email           VARCHAR(200),
    statut          VARCHAR(20)  NOT NULL DEFAULT 'OUVERT'
                    CHECK (statut IN ('OUVERT','EN_COURS','RESOLU')),
    date_creation   TIMESTAMP    NOT NULL DEFAULT NOW(),
    date_resolution TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tickets_statut ON tickets(statut);
CREATE INDEX IF NOT EXISTS idx_tickets_date   ON tickets(date_creation DESC);
