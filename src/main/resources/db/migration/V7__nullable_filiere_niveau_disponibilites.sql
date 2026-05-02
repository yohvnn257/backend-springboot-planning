-- V7 : Rendre filiere et niveau nullables sur disponibilites.
-- Raison metier : le prof saisit juste ses creneaux. La secretaire assigne filiere+niveau
-- au moment de la generation de l'EDT (cf. EmploiDuTempsService.generer).

ALTER TABLE disponibilites ALTER COLUMN filiere DROP NOT NULL;
ALTER TABLE disponibilites ALTER COLUMN niveau DROP NOT NULL;

-- Mettre a jour le CHECK pour autoriser NULL (la contrainte initiale est dans V1)
ALTER TABLE disponibilites DROP CONSTRAINT IF EXISTS disponibilites_niveau_check;
ALTER TABLE disponibilites ADD CONSTRAINT disponibilites_niveau_check
    CHECK (niveau IS NULL OR niveau IN ('L1','L2','L3','M1','M2'));
