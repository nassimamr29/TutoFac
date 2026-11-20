# TutoFac

Plateforme web inter-établissements de mutualisation et de gestion du tutorat universitaire.

TutoFac permet aux établissements partenaires de partager un réseau de tuteurs étudiants, de gérer les besoins d'accompagnement, de rechercher des tuteurs qualifiés et disponibles, puis d'organiser des séances de tutorat.

**Projet universitaire — Master 2 Informatique, parcours P2S**  
Université Sorbonne Paris Nord — Année universitaire 2026-2027.

## Présentation du projet

Le projet TutoFac vise à faciliter l'organisation du tutorat entre plusieurs établissements universitaires.

La plateforme repose sur quatre types d'utilisateurs :

| Rôle | Description |
|---|---|
| `ADMIN` | Administrateur de la plateforme : gestion des établissements et des comptes utilisateurs |
| `GESTIONNAIRE` | Référent d'établissement : validation des tuteurs, gestion des besoins et organisation des séances |
| `TUTEUR` | Étudiant proposant du tutorat : gestion de son profil académique et de ses disponibilités |
| `ETUDIANT` | Étudiant bénéficiaire du tutorat, rattaché à un établissement partenaire |

L'application utilise une authentification JWT et un contrôle des accès selon les rôles.

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Authentification | JWT |
| Base de données | PostgreSQL 16 |
| Migrations | Flyway |
| Frontend | React 18, Vite, React Router |
| Conteneurisation | Docker, Docker Compose |
| API | REST |

## Installation et lancement

### 1. Cloner le projet

```bash
git clone https://github.com/nassimamr29/TutoFac.git
cd TutoFac
git switch develop
```

La branche `develop` contient les développements intégrés de l'équipe.

### 2. Configurer les variables d'environnement

Créer le fichier `.env` à la racine du projet à partir de l'exemple fourni, qui contient des valeurs de démonstration :

```bash
cp .env.example .env
```

Les variables sont :

| Variable | Description |
|---|---|
| `POSTGRES_DB` | Nom de la base de données |
| `POSTGRES_USER` | Utilisateur PostgreSQL |
| `POSTGRES_PASSWORD` | Mot de passe PostgreSQL |
| `JWT_SECRET` | Clé utilisée pour signer les tokens JWT (32 caractères minimum) |
| `JWT_EXPIRATION_MS` | Durée de validité des tokens JWT |

Le fichier `.env` contient des informations sensibles et ne doit pas être envoyé sur GitHub.

### 3. Démarrer les services Docker

Depuis la racine du projet :

```bash
docker compose up -d --build
```

Si Docker nécessite les droits administrateur :

```bash
sudo docker compose up -d --build
```

Vérifier les services :

```bash
sudo docker compose ps
```

Le backend est accessible à l'adresse :

```text
http://localhost:8080
```

PostgreSQL est accessible sur le port `5433` de la machine hôte.

`docker compose` démarre aussi :

| Service | Adresse | Rôle |
|---|---|---|
| frontend | http://localhost:3000 | Front React buildé, servi par Nginx qui relaie `/api` vers le backend |
| mailpit | http://localhost:8025 | Boîte mail de test : tous les e-mails envoyés par TutoFac y apparaissent |

### 4. Démarrer le frontend en mode développement (optionnel)

Le front est déjà servi sur le port 3000 par Docker. Pour développer avec rechargement à chaud, dans un second terminal :

```bash
cd frontend
npm install
npm run dev
```

Ouvrir ensuite :

```text
http://localhost:5173
```

La plateforme redirige les utilisateurs non authentifiés vers la page de connexion.

### 5. Arrêter l'application

Arrêter le frontend avec `Ctrl + C`.

Puis arrêter les conteneurs :

```bash
sudo docker compose down
```

Pour supprimer également les volumes Docker du projet :

```bash
sudo docker compose down -v
```

**Attention :** la suppression des volumes entraîne la perte des données persistantes qui y sont stockées.

## Comptes de démonstration

Les comptes suivants sont disponibles dans les données de démonstration.

**Mot de passe commun :** `Demo1234!`

| Adresse e-mail | Rôle | Établissement |
|---|---|---|
| `admin@tutofac.fr` | ADMIN | Aucun |
| `gestionnaire.spn@tutofac.fr` | GESTIONNAIRE | Université Sorbonne Paris Nord |
| `gestionnaire.nanterre@tutofac.fr` | GESTIONNAIRE | Université Paris Nanterre |
| `gestionnaire.cite@tutofac.fr` | GESTIONNAIRE | Université Paris Cité |
| `tuteur1@tutofac.fr` | TUTEUR | Université Sorbonne Paris Nord |
| `tuteur4@tutofac.fr` | TUTEUR | Université Sorbonne Paris Nord |
| `etudiant1@tutofac.fr` | ETUDIANT | Université Paris Nanterre |

Le compte `tuteur4@tutofac.fr` possède un profil en statut `EN_ATTENTE`, utilisé pour les démonstrations du parcours de validation.

Les données de démonstration comprennent également d'autres tuteurs, étudiants et gestionnaires.

## Fonctionnalités

### 1. Authentification et navigation

La plateforme dispose d'un système d'authentification sécurisé par JWT.

Après connexion, chaque utilisateur accède à un espace correspondant à son rôle.

L'interface comprend :

- Une page de connexion.
- Une page d'accueil personnalisée.
- Une barre de navigation adaptée au rôle.
- Une déconnexion.
- Des routes protégées empêchant l'accès aux fonctionnalités non autorisées.

Le frontend utilise React Router pour gérer la navigation.

### 2. Espace Tuteur

L'espace Tuteur permet à un étudiant de gérer sa candidature et ses disponibilités.

**Gestion du profil académique**

Le tuteur peut :

- Renseigner sa formation.
- Sélectionner son niveau d'études.
- Choisir les matières qu'il souhaite enseigner.
- Enregistrer ou mettre à jour son profil.
- Consulter le statut de sa candidature.

Les statuts possibles sont :

| Statut | Signification |
|---|---|
| `EN_ATTENTE` | Dossier en attente de vérification |
| `VALIDE` | Profil accepté par le gestionnaire |
| `REFUSE` | Profil refusé avec un motif |

**Dépôt du relevé de notes**

L'application prévoit le dépôt d'un relevé de notes au format PDF.

Contraintes :

- Format PDF uniquement.
- Taille maximale de 5 Mo.
- Vérification du document côté backend.

**Gestion des disponibilités**

Le tuteur peut :

- Ajouter une disponibilité ponctuelle.
- Créer des disponibilités hebdomadaires récurrentes.
- Modifier un créneau.
- Supprimer un créneau.
- Consulter ses disponibilités enregistrées.

L'application contrôle la cohérence des horaires et empêche les chevauchements de disponibilités incompatibles.

### 3. Espace Gestionnaire

Le gestionnaire organise le tutorat pour son établissement.

**Validation des tuteurs**

Depuis la page « Validation des tuteurs », le gestionnaire peut :

- Consulter les candidatures en attente de son établissement.
- Examiner les informations académiques.
- Consulter les matières proposées.
- Accéder au relevé PDF lorsqu'il est disponible.
- Valider une candidature disposant d'un relevé.
- Refuser une candidature en indiquant obligatoirement un motif.

Un gestionnaire ne peut pas traiter les candidatures d'un autre établissement.

**Gestion des besoins de tutorat**

Le gestionnaire peut :

- Créer un besoin pour une matière.
- Définir le niveau d'études concerné.
- Indiquer le nombre d'étudiants.
- Définir une période de début et de fin.
- Consulter les besoins de son établissement.
- Annuler un besoin.

Chaque besoin dispose d'un statut permettant de suivre son évolution.

**Recherche de tuteurs**

La recherche repose sur :

- Le besoin de tutorat sélectionné.
- La matière concernée.
- Le niveau d'études requis.
- Le créneau horaire souhaité.
- Les disponibilités des tuteurs.
- Le statut de validation des profils.

La recherche est inter-établissements : un gestionnaire peut identifier un tuteur provenant d'une autre université partenaire.

Les résultats affichent notamment le nom du tuteur, sa formation, son niveau et son établissement.

**Affectation des séances**

Le gestionnaire peut :

- Sélectionner un besoin.
- Choisir un tuteur.
- Définir la date et les horaires.
- Préciser une salle ou un lien de visioconférence.
- Associer un ou plusieurs étudiants.
- Créer une séance.
- Consulter les séances planifiées.
- Annuler une séance.
- Marquer une séance comme réalisée.

Les séances sont associées à leur besoin de tutorat et à leur tuteur.

### 4. Espace Administrateur

L'administrateur dispose d'un tableau de bord donnant accès aux informations de la plateforme.

L'interface actuelle permet de :

- Consulter le nombre d'utilisateurs enregistrés.
- Consulter le nombre d'établissements partenaires.
- Afficher les utilisateurs avec leurs noms, prénoms, e-mails et rôles.
- Consulter les établissements enregistrés.

Le backend expose également des opérations d'administration des comptes et des établissements.

Les formulaires de création et de modification ne sont pas encore intégrés à cette interface React.

### 5. Espace Étudiant

L'étudiant dispose d'un espace personnel permettant de :

- Consulter son nom et son prénom.
- Consulter son adresse e-mail.
- Identifier son rôle.
- Consulter son établissement de rattachement.
- Découvrir les établissements partenaires.

Dans la version actuelle, les besoins de tutorat et les séances sont organisés par les gestionnaires d'établissement.

La création directe de demandes et l'inscription autonome aux séances ne sont pas encore disponibles dans l'interface étudiant.

## API REST

Le backend expose son API à l'adresse :

```text
http://localhost:8080
```

Les routes protégées nécessitent un token JWT :

```http
Authorization: Bearer <token>
```

### Authentification

| Méthode | Route | Fonction |
|---|---|---|
| `POST` | `/api/auth/login` | Authentification |

### Utilisateurs et établissements

| Méthode | Route | Fonction |
|---|---|---|
| `GET` | `/api/utilisateurs/me` | Informations de l'utilisateur connecté |
| `GET` | `/api/utilisateurs` | Liste des utilisateurs, ADMIN ou GESTIONNAIRE |
| `POST` | `/api/utilisateurs` | Création d'un utilisateur, ADMIN |
| `GET` | `/api/etablissements` | Liste des établissements |
| `GET` | `/api/etablissements/{id}` | Détail d'un établissement |
| `POST` | `/api/etablissements` | Création, ADMIN |
| `PUT` | `/api/etablissements/{id}` | Modification, ADMIN |
| `DELETE` | `/api/etablissements/{id}` | Suppression, ADMIN |

### Tuteurs

| Méthode | Route | Fonction |
|---|---|---|
| `GET` | `/api/tuteurs/me` | Consulter son profil |
| `PUT` | `/api/tuteurs/me` | Enregistrer son profil |
| `POST` | `/api/tuteurs/me/releve` | Déposer un relevé PDF |
| `GET` | `/api/tuteurs/a-valider` | Lister les candidatures en attente |
| `POST` | `/api/tuteurs/{profilId}/decision` | Valider ou refuser |
| `GET` | `/api/tuteurs/{profilId}/releve` | Consulter un relevé PDF |
| `GET` | `/api/matieres` | Consulter les matières |

Exemple de validation :

```json
{
  "decision": "VALIDE"
}
```

Exemple de refus :

```json
{
  "decision": "REFUSE",
  "motifRefus": "Le relevé de notes est incomplet."
}
```

### Disponibilités

| Méthode | Route | Fonction |
|---|---|---|
| `GET` | `/api/disponibilites` | Consulter ses disponibilités |
| `POST` | `/api/disponibilites` | Ajouter une disponibilité |
| `PUT` | `/api/disponibilites/{id}` | Modifier une disponibilité |
| `DELETE` | `/api/disponibilites/{id}` | Supprimer une disponibilité |

Les disponibilités peuvent être ponctuelles ou hebdomadaires.

### Besoins de tutorat

| Méthode | Route | Fonction |
|---|---|---|
| `GET` | `/api/besoins` | Liste des besoins de l'établissement |
| `GET` | `/api/besoins/{id}` | Détail d'un besoin |
| `POST` | `/api/besoins` | Création d'un besoin |
| `PATCH` | `/api/besoins/{id}/annuler` | Annulation d'un besoin |
| `GET` | `/api/besoins/{id}/tuteurs` | Recherche des tuteurs disponibles |

La recherche des tuteurs utilise les paramètres `debut` et `fin` au format ISO 8601.

### Séances

| Méthode | Route | Fonction |
|---|---|---|
| `GET` | `/api/seances` | Liste des séances de l'établissement |
| `GET` | `/api/seances/{id}` | Détail d'une séance |
| `GET` | `/api/seances/besoin/{besoinId}` | Séances associées à un besoin |
| `POST` | `/api/seances` | Création d'une séance |
| `PATCH` | `/api/seances/{id}/annuler` | Annulation d'une séance |
| `PATCH` | `/api/seances/{id}/realiser` | Marquer une séance comme réalisée |

Les opérations liées aux besoins et aux séances sont réservées au rôle `GESTIONNAIRE`.

## Base de données

Le schéma PostgreSQL est géré par Flyway.

Les migrations sont disponibles dans :

```text
backend/src/main/resources/db/migration/
```

| Migration | Description |
|---|---|
| `V1__init.sql` | Création du schéma initial |
| `V2__seed.sql` | Insertion des données de démonstration |

Le schéma initial comprend neuf tables :

1. `etablissement`
2. `utilisateur`
3. `matiere`
4. `profil_tuteur`
5. `profil_tuteur_matiere`
6. `disponibilite`
7. `besoin_tutorat`
8. `seance`
9. `participation`

Les migrations déjà appliquées ne doivent pas être modifiées directement.

Toute évolution de la base doit faire l'objet d'une nouvelle migration Flyway.

## Organisation du frontend

Le frontend React est organisé autour des dossiers suivants :

```text
frontend/src/
├── api/
│   ├── besoin.js
│   ├── client.js
│   ├── disponibilite.js
│   ├── seance.js
│   └── tuteur.js
├── auth/
│   ├── AuthContext.jsx
│   └── Login.jsx
├── components/
│   ├── Layout.jsx
│   ├── Navbar.jsx
│   └── ProtectedRoute.jsx
├── pages/
│   ├── Accueil.jsx
│   ├── Administration.jsx
│   ├── Affectation.jsx
│   ├── Besoin.jsx
│   ├── Disponibilites.jsx
│   ├── EspaceEtudiant.jsx
│   ├── ProfilTuteur.jsx
│   ├── Recherche.jsx
│   └── ValidationTuteurs.jsx
├── App.jsx
├── main.jsx
└── style.css
```

La navigation utilise React Router.

Les pages sont protégées selon le rôle de l'utilisateur connecté.

L'interface graphique adopte une présentation commune avec :

- Une navigation adaptée à chaque rôle.
- Des tableaux de bord personnalisés.
- Des formulaires harmonisés.
- Des cartes de navigation.
- Des tableaux de consultation.
- Une mise en page responsive.

## Notifications par e-mail

Le module `notification/` envoie des e-mails en texte brut (Spring Mail). En local, ils sont capturés par Mailpit et visibles sur http://localhost:8025.

| Événement | Destinataires | Déclenchement |
|---|---|---|
| Affectation | Tuteur et étudiants inscrits | Création d'une séance (`POST /api/seances`) |
| Rappel | Tuteur et étudiants inscrits | Chaque jour à 18h, pour les séances planifiées du lendemain |
| Modification | Tuteur et étudiants inscrits | `NotificationService.notifierModification(seance)` |
| Annulation | Tuteur et étudiants inscrits | `NotificationService.notifierAnnulation(seance)` |

Les e-mails partent seulement après la validation de la transaction, et un échec d'envoi est journalisé sans faire échouer l'action. Variables : `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `MAIL_RAPPEL_CRON`.

## Tests et vérifications

La compilation du frontend peut être vérifiée avec :

```bash
npm run build --prefix frontend
```

Les tests du backend demandent Java 21. Sans JDK 21 installé, ils se lancent dans un conteneur Maven, depuis la racine du projet :

```bash
docker run --rm -v "$PWD/backend":/app -w /app maven:3.9-eclipse-temurin-21 mvn test
```

Avec Java 21 et Maven installés, depuis le dossier `backend` :

```bash
mvn test
```

Les fonctionnalités suivantes ont fait l'objet de vérifications manuelles :

- Connexion avec différents rôles.
- Accès aux pages autorisées.
- Création et consultation des besoins.
- Recherche inter-établissements de tuteurs.
- Création de séances.
- Ajout d'une disponibilité.
- Consultation des candidatures en attente.
- Consultation des utilisateurs et établissements.
- Consultation de l'espace étudiant.

### Points restant à vérifier ou à améliorer

- Corriger l'anomalie rencontrée lors du dépôt d'un relevé PDF depuis l'interface tuteur.
- Vérifier complètement les décisions de validation et de refus des candidatures.
- Renforcer les tests des conflits de réservation, notamment lorsqu'un même étudiant apparaît sur plusieurs séances simultanées.
- Compléter les interfaces d'administration si nécessaire.
- Ajouter les fonctionnalités étudiantes supplémentaires selon le périmètre retenu.
- Compléter les tests automatisés des services métier.

## Avancement du projet

| Partie | État |
|---|---|
| 1. Socle, sécurité et comptes | Implémentée : Docker, PostgreSQL, Flyway, authentification JWT, rôles, utilisateurs et établissements |
| 2. Tuteurs et disponibilités | Implémentée : profils académiques, matières, disponibilités, API de validation et interface gestionnaire |
| 3. Besoins, recherche et affectation | Implémentée : gestion des besoins, recherche inter-établissements, création et suivi des séances |
| 4. Frontend global et navigation | Implémentée : connexion, navigation par rôle, tableaux de bord, pages gestionnaire, administrateur et étudiant |
| 4. Notifications | Implémentée : e-mails d'affectation, rappel, modification et annulation (Mailpit en local) |

Les fonctionnalités principales du MVP sont intégrées. Certains parcours restent à tester ou à compléter avant la livraison définitive.

## Documentation

Le projet est accompagné de documents de conception et d'utilisation :

- Cahier des charges.
- Documentation technique.
- Guide utilisateur.
- Scénarios de démonstration et de validation.

## Équipe

Projet réalisé par :

- Nassim Amrouche
- Yassine Berriche
- Rayane Harkati
- Saad Mimouni

Projet hébergé sur Vercel

**Master 2 Informatique — Parcours P2S**  
**Université Sorbonne Paris Nord — 2026-2027**