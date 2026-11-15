CREATE TABLE etablissement (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL
        CHECK (type IN ('UNIVERSITE','ECOLE_INGENIEUR','ECOLE_COMMERCE','PRIVE','AUTRE')),
    statut_partenariat VARCHAR(20) NOT NULL DEFAULT 'ACTIF'
        CHECK (statut_partenariat IN ('ACTIF','SUSPENDU')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE utilisateur (
    id BIGSERIAL PRIMARY KEY,
    etablissement_id BIGINT REFERENCES etablissement(id),
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
        CHECK (role IN ('ADMIN','GESTIONNAIRE','TUTEUR','ETUDIANT')),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    -- seul l'ADMIN peut ne pas avoir d'établissement
    CHECK (role = 'ADMIN' OR etablissement_id IS NOT NULL)
);
CREATE INDEX idx_utilisateur_etablissement ON utilisateur(etablissement_id);

CREATE TABLE matiere (
    id BIGSERIAL PRIMARY KEY,
    intitule VARCHAR(150) NOT NULL,
    niveau VARCHAR(20) NOT NULL
        CHECK (niveau IN ('L1','L2','L3','M1','M2')),
    domaine VARCHAR(100) NOT NULL,
    UNIQUE (intitule, niveau)
);

CREATE TABLE profil_tuteur (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    formation VARCHAR(150) NOT NULL,
    niveau_etudes VARCHAR(20) NOT NULL
        CHECK (niveau_etudes IN ('L1','L2','L3','M1','M2')),
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
        CHECK (statut IN ('EN_ATTENTE','VALIDE','REFUSE')),
    releve_path VARCHAR(255),
    motif_refus VARCHAR(500),
    valide_par BIGINT REFERENCES utilisateur(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE profil_tuteur_matiere (
    profil_tuteur_id BIGINT NOT NULL REFERENCES profil_tuteur(id) ON DELETE CASCADE,
    matiere_id BIGINT NOT NULL REFERENCES matiere(id),
    PRIMARY KEY (profil_tuteur_id, matiere_id)
);
CREATE INDEX idx_ptm_matiere ON profil_tuteur_matiere(matiere_id);

CREATE TABLE disponibilite (
    id BIGSERIAL PRIMARY KEY,
    profil_tuteur_id BIGINT NOT NULL REFERENCES profil_tuteur(id) ON DELETE CASCADE,
    debut TIMESTAMP NOT NULL,
    fin TIMESTAMP NOT NULL,
    recurrence VARCHAR(20) NOT NULL DEFAULT 'AUCUNE'
        CHECK (recurrence IN ('AUCUNE','HEBDOMADAIRE')),
    fin_recurrence DATE,
    CHECK (fin > debut)
);
CREATE INDEX idx_dispo_tuteur ON disponibilite(profil_tuteur_id, debut);

CREATE TABLE besoin_tutorat (
    id BIGSERIAL PRIMARY KEY,
    etablissement_id BIGINT NOT NULL REFERENCES etablissement(id),
    matiere_id BIGINT NOT NULL REFERENCES matiere(id),
    cree_par BIGINT NOT NULL REFERENCES utilisateur(id),
    niveau VARCHAR(20) NOT NULL
        CHECK (niveau IN ('L1','L2','L3','M1','M2')),
    effectif INT NOT NULL CHECK (effectif > 0),
    periode_debut DATE NOT NULL,
    periode_fin DATE NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'OUVERT'
        CHECK (statut IN ('OUVERT','COUVERT','ANNULE')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (periode_fin >= periode_debut)
);
CREATE INDEX idx_besoin_etablissement ON besoin_tutorat(etablissement_id);

CREATE TABLE seance (
    id BIGSERIAL PRIMARY KEY,
    besoin_id BIGINT NOT NULL REFERENCES besoin_tutorat(id),
    profil_tuteur_id BIGINT NOT NULL REFERENCES profil_tuteur(id),
    debut TIMESTAMP NOT NULL,
    fin TIMESTAMP NOT NULL,
    lieu_ou_lien VARCHAR(255),
    statut VARCHAR(20) NOT NULL DEFAULT 'PLANIFIEE'
        CHECK (statut IN ('PLANIFIEE','REALISEE','ANNULEE')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (fin > debut)
);
CREATE INDEX idx_seance_tuteur ON seance(profil_tuteur_id, debut);
CREATE INDEX idx_seance_besoin ON seance(besoin_id);

CREATE TABLE participation (
    id BIGSERIAL PRIMARY KEY,
    seance_id BIGINT NOT NULL REFERENCES seance(id) ON DELETE CASCADE,
    etudiant_id BIGINT NOT NULL REFERENCES utilisateur(id),
    presence BOOLEAN,
    commentaire VARCHAR(500),
    UNIQUE (seance_id, etudiant_id)
);