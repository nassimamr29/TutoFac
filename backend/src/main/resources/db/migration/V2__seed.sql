CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO etablissement (nom, type) VALUES
  ('Université Sorbonne Paris Nord', 'UNIVERSITE'),
  ('Université Paris Nanterre', 'UNIVERSITE'),
  ('Université Paris Cité', 'UNIVERSITE');

INSERT INTO matiere (intitule, niveau, domaine) VALUES
  ('Algorithmique', 'L2', 'Informatique'),
  ('Bases de données', 'L3', 'Informatique'),
  ('Analyse', 'L1', 'Mathématiques'),
  ('Probabilités', 'L2', 'Mathématiques'),
  ('Anglais', 'L1', 'Langues');

INSERT INTO utilisateur (etablissement_id, nom, prenom, email, mot_de_passe, role)
SELECT e.id, v.nom, v.prenom, v.email, 'x', v.role
FROM (VALUES
  (NULL,                              'Admin',   'TutoFac', 'admin@tutofac.fr',                'ADMIN'),
  ('Université Sorbonne Paris Nord',  'Martin',  'Claire',  'gestionnaire.spn@tutofac.fr',     'GESTIONNAIRE'),
  ('Université Paris Nanterre',       'Durand',  'Paul',    'gestionnaire.nanterre@tutofac.fr','GESTIONNAIRE'),
  ('Université Paris Cité',           'Petit',   'Sofia',   'gestionnaire.cite@tutofac.fr',    'GESTIONNAIRE'),
  ('Université Sorbonne Paris Nord',  'Bernard', 'Lucas',   'tuteur1@tutofac.fr',              'TUTEUR'),
  ('Université Paris Nanterre',       'Moreau',  'Inès',    'tuteur2@tutofac.fr',              'TUTEUR'),
  ('Université Paris Cité',           'Garcia',  'Yanis',   'tuteur3@tutofac.fr',              'TUTEUR'),
  ('Université Sorbonne Paris Nord',  'Roux',    'Emma',    'tuteur4@tutofac.fr',              'TUTEUR'),
  ('Université Paris Nanterre',       'Leroy',   'Hugo',    'etudiant1@tutofac.fr',            'ETUDIANT'),
  ('Université Paris Nanterre',       'Simon',   'Lina',    'etudiant2@tutofac.fr',            'ETUDIANT'),
  ('Université Paris Cité',           'Faure',   'Adam',    'etudiant3@tutofac.fr',            'ETUDIANT'),
  ('Université Sorbonne Paris Nord',  'Blanc',   'Nora',    'etudiant4@tutofac.fr',            'ETUDIANT')
) AS v(etab, nom, prenom, email, role)
LEFT JOIN etablissement e ON e.nom = v.etab;

UPDATE utilisateur SET mot_de_passe = crypt('Demo1234!', gen_salt('bf', 10));

-- Profils tuteurs (tuteur4 reste EN_ATTENTE pour démontrer la validation)
INSERT INTO profil_tuteur (utilisateur_id, formation, niveau_etudes, statut)
SELECT u.id, v.formation, v.niveau, v.statut
FROM (VALUES
  ('tuteur1@tutofac.fr', 'Master Informatique', 'M1', 'VALIDE'),
  ('tuteur2@tutofac.fr', 'Licence Mathématiques', 'L3', 'VALIDE'),
  ('tuteur3@tutofac.fr', 'Master Informatique', 'M2', 'VALIDE'),
  ('tuteur4@tutofac.fr', 'Licence Informatique', 'L3', 'EN_ATTENTE')
) AS v(email, formation, niveau, statut)
JOIN utilisateur u ON u.email = v.email;

-- Matières validées par chaque tuteur
INSERT INTO profil_tuteur_matiere (profil_tuteur_id, matiere_id)
SELECT p.id, m.id
FROM (VALUES
  ('tuteur1@tutofac.fr', 'Algorithmique', 'L2'),
  ('tuteur1@tutofac.fr', 'Bases de données', 'L3'),
  ('tuteur2@tutofac.fr', 'Analyse', 'L1'),
  ('tuteur2@tutofac.fr', 'Probabilités', 'L2'),
  ('tuteur3@tutofac.fr', 'Algorithmique', 'L2'),
  ('tuteur3@tutofac.fr', 'Anglais', 'L1'),
  ('tuteur4@tutofac.fr', 'Algorithmique', 'L2')
) AS v(email, intitule, niveau)
JOIN utilisateur u ON u.email = v.email
JOIN profil_tuteur p ON p.utilisateur_id = u.id
JOIN matiere m ON m.intitule = v.intitule AND m.niveau = v.niveau;

-- Disponibilités : semaine prochaine, pour que la démo reste d'actualité
INSERT INTO disponibilite (profil_tuteur_id, debut, fin, recurrence)
SELECT p.id,
       date_trunc('week', now()) + interval '1 week' + v.decalage,
       date_trunc('week', now()) + interval '1 week' + v.decalage + interval '2 hours',
       'AUCUNE'
FROM (VALUES
  ('tuteur1@tutofac.fr', interval '1 day 14 hours'),
  ('tuteur1@tutofac.fr', interval '3 days 10 hours'),
  ('tuteur2@tutofac.fr', interval '2 days 16 hours'),
  ('tuteur3@tutofac.fr', interval '1 day 14 hours'),
  ('tuteur3@tutofac.fr', interval '4 days 9 hours')
) AS v(email, decalage)
JOIN utilisateur u ON u.email = v.email
JOIN profil_tuteur p ON p.utilisateur_id = u.id;