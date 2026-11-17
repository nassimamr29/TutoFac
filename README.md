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
| Frontend | React + Vite |
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

4. Dans un second terminal, lancer le frontend :

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   Ouvrir `http://localhost:5173` et se connecter avec un compte TUTEUR de
   démonstration, par exemple `tuteur1@tutofac.fr` / `Demo1234!`.

5. Arrêter :

   ```bash
   docker compose down        # conserve les données
   docker compose down -v     # supprime la base et les relevés (repart de zéro)
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
Les relevés PDF sont limités à 5 Mo et conservés dans le volume Docker `releves`.

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


### Routes tuteurs et disponibilités

| Méthode | Route | Accès |
|---|---|---|
| `GET` | `/api/matieres` | Utilisateur authentifié |
| `GET`, `PUT` | `/api/tuteurs/me` | Tuteur |
| `POST` | `/api/tuteurs/me/releve` | Tuteur, multipart `fichier` PDF ≤ 5 Mo |
| `GET` | `/api/tuteurs/a-valider` | Gestionnaire, établissement courant |
| `POST` | `/api/tuteurs/{id}/decision` | Gestionnaire du même établissement |
| `GET`, `POST`, `PUT`, `DELETE` | `/api/disponibilites` | Tuteur, uniquement ses créneaux |

Une décision de validation se fait avec `{"decision":"VALIDE"}`. Pour refuser,
envoyer `{"decision":"REFUSE","motifRefus":"..."}`. Une disponibilité
hebdomadaire utilise `recurrence: "HEBDOMADAIRE"` et une `finRecurrence` au
format `YYYY-MM-DD`; un créneau ponctuel utilise `recurrence: "AUCUNE"`.

### Fonctionnalités de la partie 2

- Le tuteur complète son profil (formation, niveau d'études et matières) et
  dépose son relevé de notes au format PDF.
- Le gestionnaire consulte les profils à valider de son établissement et peut
  les valider ou les refuser avec un motif.
- Le tuteur crée, modifie et supprime ses disponibilités ponctuelles ou
  hebdomadaires. Les horaires invalides et les chevauchements sont refusés.
- Les pages et la navigation tiennent compte du rôle connecté : les pages de
  profil et de disponibilités sont réservées aux tuteurs. Les autres rôles
  arrivent sur leur espace d'accueil.

## Avancement

| Partie | État |
|---|---|
| 1. Socle, sécurité, comptes | Terminée : schéma Flyway, seed, Docker, JWT, rôles, établissements, utilisateurs, gestion d'erreurs |
| 2. Tuteurs et disponibilités | Terminée : profils, validation par établissement, relevés PDF, matières et créneaux ponctuels/hebdomadaires |
| 3. Besoins, recherche, affectation | À faire |
| 4. Front global, notifications | À faire |

## Équipe

Nassim Amrouche, Yassine Berriche, Rayane Harkati, Saad Mimouni.