INSERT INTO professeurs (nom, matiere, email, telephone, whatsapp_statut) VALUES
  ('M. KOUASSI Jean-Marc', 'Développement Web', 'kouassi@isd.ci', '+2250700000001', 'ATTENTE'),
  ('Mme. DIALLO Fatoumata', 'Base de données', 'diallo@isd.ci', '+2250700000002', 'ATTENTE'),
  ('M. BAMBA Seydou', 'Algorithmes', 'bamba@isd.ci', '+2250700000003', 'ATTENTE')
ON CONFLICT DO NOTHING;

INSERT INTO etudiants (nom, email, filiere, niveau) VALUES
  ('Koné Aminata', 'kone@etudiant.isd.ci', 'Développement Web', 'L3'),
  ('Traoré Ibrahim', 'traore@etudiant.isd.ci', 'Développement Web', 'L3'),
  ('Coulibaly Mariam', 'coulibaly@etudiant.isd.ci', 'Base de données', 'L2')
ON CONFLICT DO NOTHING;
