# Guide utilisateur TutoFac

TutoFac est accessible à l'adresse `http://localhost:3000` une fois l'application
lancée avec `docker compose up --build`. Chaque utilisateur se connecte avec son
adresse e-mail et son mot de passe. Le menu affiché dépend du rôle du compte.

<!-- Chapitres Tuteur (partie 2) et Gestionnaire (partie 3) à compléter par leurs auteurs -->

## Chapitre Étudiant

### À qui s'adresse ce chapitre

Aux étudiants orientés vers le tutorat par leur établissement. L'étudiant ne
choisit pas lui-même ses séances : c'est le gestionnaire de son établissement
qui l'inscrit à une séance avec un tuteur.

### Se connecter

1. Ouvrir `http://localhost:3000`. La page **Connexion TutoFac** s'affiche.
2. Saisir son adresse e-mail et son mot de passe, puis cliquer sur
   **Se connecter**.
3. En cas d'erreur, le message « Email ou mot de passe incorrect » s'affiche.
   Le message est volontairement le même si l'adresse n'existe pas ou si le
   compte est désactivé : contacter le gestionnaire de son établissement.

Compte de démonstration : `etudiant4@tutofac.fr` / `Demo1234!`
(Université Sorbonne Paris Nord).

### L'espace étudiant

Après connexion, l'étudiant arrive sur la page **Espace étudiant**. La barre du
haut affiche le nom de son établissement et le bouton **Déconnexion**.

Dans cette version, l'espace étudiant ne propose pas encore de liste de séances
en ligne : les informations arrivent par e-mail (voir ci-dessous).

### Recevoir une convocation à une séance

Quand le gestionnaire inscrit l'étudiant à une séance, TutoFac envoie
automatiquement un e-mail :

- **Objet :** « TutoFac : vous êtes inscrit(e) à une séance de tutorat le JJ/MM/AAAA »
- **Contenu :** la date, l'horaire (début et fin) et le lieu ou le lien de
  visioconférence. Si le lieu n'est pas encore connu, il est indiqué
  « à préciser ».

L'e-mail n'est envoyé que si la séance a bien été enregistrée. En cas
d'absence prévue, prévenir son établissement : l'adresse d'envoi
(`noreply@tutofac.fr`) ne lit pas les réponses.

En environnement de démonstration, les e-mails ne partent pas réellement : ils
sont visibles dans la boîte de test Mailpit, à l'adresse `http://localhost:8025`.

### Session expirée et déconnexion

- La connexion reste valable 24 heures. Passé ce délai, la prochaine action
  renvoie automatiquement vers la page de connexion avec une session vide :
  il suffit de se reconnecter.
- Le bouton **Déconnexion** ferme la session sur le navigateur utilisé. Penser
  à l'utiliser sur un ordinateur partagé.

## Chapitre Administrateur

### À qui s'adresse ce chapitre

À l'équipe TutoFac. L'administrateur n'est rattaché à aucun établissement : il
gère les établissements partenaires et crée les comptes. Il n'intervient pas
sur les besoins, les tuteurs ni les séances, qui relèvent des gestionnaires.

Compte de démonstration : `admin@tutofac.fr` / `Demo1234!`

### Se connecter

La connexion se fait comme pour les autres rôles sur `http://localhost:3000`.
L'administrateur arrive sur la page **Espace administrateur**. Dans cette
version, les écrans d'administration ne sont pas encore disponibles dans
l'interface web : les opérations se font par l'API, décrites ci-dessous.

### Obtenir un jeton de connexion

Toutes les opérations demandent le jeton renvoyé par la connexion :

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@tutofac.fr","motDePasse":"Demo1234!"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

Le jeton est valable 24 heures. Une réponse `401` signifie qu'il est absent ou
expiré : refaire la commande ci-dessus.

### Gérer les établissements partenaires

| Action | Commande |
|---|---|
| Lister | `GET /api/etablissements` |
| Consulter | `GET /api/etablissements/{id}` |
| Créer | `POST /api/etablissements` |
| Modifier | `PUT /api/etablissements/{id}` |
| Supprimer | `DELETE /api/etablissements/{id}` |

Exemple de création :

```bash
curl -X POST localhost:8080/api/etablissements \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"nom":"École Exemple","type":"ECOLE_INGENIEUR","statutPartenariat":"ACTIF"}'
```

- `type` vaut `UNIVERSITE`, `ECOLE_INGENIEUR`, `ECOLE_COMMERCE`, `PRIVE` ou `AUTRE`.
- `statutPartenariat` vaut `ACTIF` (par défaut) ou `SUSPENDU`. Pour mettre fin à
  un partenariat, préférer `SUSPENDU` à la suppression : l'historique est conservé.
- Deux établissements ne peuvent pas porter le même nom (réponse `409`).
- Un établissement qui a déjà des comptes ou des besoins ne peut pas être
  supprimé (réponse `409`).

### Créer un compte utilisateur

| Action | Commande |
|---|---|
| Lister les comptes | `GET /api/utilisateurs` |
| Créer un compte | `POST /api/utilisateurs` |

Exemple : créer le gestionnaire d'un établissement.

```bash
curl -X POST localhost:8080/api/utilisateurs \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"nom":"Martin","prenom":"Claire","email":"claire.martin@exemple.fr",
       "motDePasse":"MotDePasse1","role":"GESTIONNAIRE","etablissementId":1}'
```

- `role` vaut `ADMIN`, `GESTIONNAIRE`, `TUTEUR` ou `ETUDIANT`.
- `etablissementId` est obligatoire pour tous les rôles sauf `ADMIN`.
- Le mot de passe doit faire au moins 8 caractères. Il est stocké chiffré et
  n'apparaît jamais dans les réponses.
- Une adresse e-mail déjà utilisée est refusée (réponse `409`).

### Codes de réponse

| Code | Signification |
|---|---|
| `200` / `201` / `204` | Opération réussie (`204` : suppression, sans contenu) |
| `400` | Donnée manquante ou invalide (le message précise le champ) |
| `401` | Non connecté ou jeton expiré |
| `403` | Action non autorisée pour ce rôle |
| `404` | Élément introuvable |
| `409` | Conflit : doublon ou élément encore utilisé |
