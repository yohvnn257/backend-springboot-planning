-- V6 : Suppression du champ "matiere" de la table professeurs.
-- Raison metier : un prof peut enseigner plusieurs modules a l'ISD,
-- la notion de "matiere fixe" n'a pas de sens. La table modules (V3) couvre ce besoin.
ALTER TABLE professeurs DROP COLUMN IF EXISTS matiere;
