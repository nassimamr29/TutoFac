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
| `V2__seed.sql` | Données de démo |

Tables : `etablissement`, `utilisateur`, `matiere`, `profil_tuteur`,
`profil_tuteur_matiere`, `disponibilite`, `besoin_tutorat`, `seance`,
`participation`.

Conventions pour les entités JPA :

- Rôles et statuts : `@Enumerated(EnumType.STRING)` avec les valeurs exactes du SQL.
- Dates : `LocalDateTime` (colonnes `TIMESTAMP` sans fuseau).
- `seance.profil_tuteur_id` référence `profil_tuteur`, pas `utilisateur`.
- Un `ADMIN` n'a pas d'établissement, les autres rôles en ont un.
- Ne jamais modifier une migration déjà appliquée : créer une `V3__...sql`.

#### Comptes de démo

Mot de passe commun : `Demo1234!`

| Email | Rôle | Établissement |
|---|---|---|
| `admin@tutofac.fr` | ADMIN | aucun |
| `gestionnaire.spn@tutofac.fr` | GESTIONNAIRE | Sorbonne Paris Nord |
| `gestionnaire.nanterre@tutofac.fr` | GESTIONNAIRE | Paris Nanterre |
| `gestionnaire.cite@tutofac.fr` | GESTIONNAIRE | Paris Cité |
| `tuteur1@tutofac.fr` à `tuteur4@tutofac.fr` | TUTEUR | SPN, Nanterre, Cité, SPN |
| `etudiant1@tutofac.fr` à `etudiant4@tutofac.fr` | ETUDIANT | Nanterre, Nanterre, Cité, SPN |

`tuteur4` est en statut `EN_ATTENTE` pour démontrer la validation d'un profil.

## API

L'API tourne sur `http://localhost:8080`. Les routes protégées demandent
l'en-tête `Authorization: Bearer <token>`. Les erreurs ont la forme
`{"status": 403, "message": "Accès refusé"}`.

Exemple :

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@tutofac.fr","motDePasse":"Demo1234!"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
curl localhost:8080/api/utilisateurs/me -H "Authorization: Bearer $TOKEN"
```


## Avancement

| Partie | État |
|---|---|
| 1. Socle, sécurité, comptes | Terminée : schéma Flyway, seed, Docker, JWT, rôles, établissements, utilisateurs, gestion d'erreurs |
| 2. Tuteurs et disponibilités | À faire |
| 3. Besoins, recherche, affectation | À faire |
| 4. Front global, notifications | À faire |

## Équipe

Nassim Amrouche, Yassine Berriche, Rayane Herkati, Saad Mimouni.