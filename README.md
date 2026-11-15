# TutoFac

Plateforme web inter-établissements de mutualisation et de gestion du tutorat
universitaire. Les établissements partenaires partagent un réseau de tuteurs
étudiants validés, les affectent à des séances et suivent leur déroulement.

Projet du Master Informatique (P2S), Université Sorbonne Paris Nord, 2026-2027.

## Rôles

| Rôle | Description |
|---|---|
| `ADMIN` | Équipe TutoFac : gère les établissements et les comptes |
| `GESTIONNAIRE` | Référent tutorat d'un établissement : crée les besoins, valide et affecte les tuteurs |
| `TUTEUR` | Étudiant d'un établissement partenaire : renseigne son profil et ses disponibilités |
| `ETUDIANT` | Étudiant orienté vers le tutorat par son établissement |

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA |
| Base de données | PostgreSQL 16, migrations Flyway |
| Frontend | React + Vite *(en cours)* |
| Déploiement | Docker, Docker Compose |



## Installation et lancement

1. Cloner le dépôt et se placer sur `develop` :

   ```bash
   git clone git@github.com:nassimamr29/TutoFac.git
   cd TutoFac

   ```

2. Créer le fichier de configuration :

   ```bash
        .env
   ```


3. Lancer l'application :

   ```bash
   docker compose up --build
   ```

   Le backend attend que PostgreSQL soit prêt, puis Flyway crée le schéma
   automatiquement au démarrage.

4. Arrêter :

   ```bash
   docker compose down        # conserve les données
   docker compose down -v     # supprime aussi la base (repart de zéro)
   ```

## Configuration

Toute la configuration passe par des variables d'environnement (fichier `.env`,
jamais commité).

| Variable | Rôle |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base de données |
| `JWT_SECRET` | Clé de signature des tokens (32 caractères minimum) |
| `JWT_EXPIRATION_MS` | Durée de vie d'un token (défaut : 24 h) |

La base est exposée sur le port **5433** de la machine (5432 dans le réseau
Docker), pour éviter un conflit avec un PostgreSQL installé en local.

## Base de données

Le schéma est géré uniquement par Flyway :
`backend/src/main/resources/db/migration/`

| Migration | Contenu |
|---|---|
| `V1__init.sql` | Schéma complet du MVP (9 tables) |
| `V2__seed.sql` | Données de démo *(à compléter)* |

Tables : `etablissement`, `utilisateur`, `matiere`, `profil_tuteur`,
`profil_tuteur_matiere`, `disponibilite`, `besoin_tutorat`, `seance`,
`participation`.

Conventions pour les entités JPA :

- Rôles et statuts : `@Enumerated(EnumType.STRING)` avec les valeurs exactes du SQL.
- Dates : `LocalDateTime` (colonnes `TIMESTAMP` sans fuseau).
- `seance.profil_tuteur_id` référence `profil_tuteur`, pas `utilisateur`.
- Un `ADMIN` n'a pas d'établissement, les autres rôles en ont un.
- Ne jamais modifier une migration déjà appliquée : créer une `V3__...sql`.

## Comptes de démo *(à compléter avec V2__seed.sql)*

## API *(à compléter)*

## Tests *(à compléter)*

```bash
cd backend && mvn test
```

## Structure du dépôt

```
tutofac/
├── docker-compose.yml
├── .env.example
├── backend/                  Spring Boot (Java 21, Maven)
│   └── src/main/java/fr/tutofac/
│       ├── config/           sécurité, CORS
│       ├── auth/             login, JWT, rôles
│       ├── etablissement/
│       ├── utilisateur/
│       ├── tuteur/           profil, relevés, validation
│       ├── disponibilite/
│       ├── besoin/           besoins et recherche
│       ├── seance/           affectation, planning
│       └── notification/
│   └── src/main/resources/db/migration/
├── frontend/                 React + Vite
└── docs/                     cahier des charges, guide, plaquette, rapports
```

## Équipe

Nassim Amrouche, Yassine Berriche, Rayane Herkati, Saad Mimouni.