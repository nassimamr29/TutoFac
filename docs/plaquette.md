# TutoFac

**Le réseau de tuteurs étudiants partagé entre établissements.**

---

## Le constat

Chaque établissement recrute ses tuteurs de son côté. Certains manquent de
tuteurs dans une matière pendant que, dans l'université voisine, des étudiants
compétents restent disponibles. Les besoins se gèrent par e-mail et tableur,
sans vue d'ensemble sur qui est qualifié ni sur qui est libre.

## La solution

TutoFac met en commun un réseau de **tuteurs étudiants validés** entre
établissements partenaires. Un gestionnaire décrit son besoin, trouve en
quelques clics un tuteur qualifié et disponible, même dans un autre
établissement, et planifie la séance. Tuteur et étudiants sont prévenus
automatiquement par e-mail.

## Comment ça marche

1. **Le tuteur s'inscrit** : formation, niveau, matières maîtrisées, relevé de
   notes en PDF et créneaux de disponibilité.
2. **Son établissement le valide** : seul un tuteur validé peut être proposé.
3. **Le gestionnaire publie un besoin** : matière, niveau, nombre d'étudiants,
   période.
4. **TutoFac propose les tuteurs** qualifiés dans la matière et libres sur le
   créneau demandé.
5. **La séance est planifiée** sans double réservation possible, et chacun
   reçoit sa convocation par e-mail.

## Pour qui

| | Ce que TutoFac lui apporte |
|---|---|
| **Gestionnaire tutorat** | Un vivier de tuteurs plus large, une recherche par matière et créneau, un suivi des séances |
| **Tuteur** | Un profil unique valable dans tout le réseau, des créneaux gérés en ligne, des séances qui respectent son emploi du temps |
| **Étudiant** | Un accompagnement plus rapide, une convocation claire (date, horaire, lieu) |
| **Équipe TutoFac** | La gestion des établissements partenaires et des comptes |

## Confiance et confidentialité

- **Tuteurs vérifiés** : validation par l'établissement d'origine sur relevé de
  notes.
- **Chaque établissement chez soi** : un gestionnaire ne voit que les données de
  son établissement.
- **Accès sécurisé** : connexion par compte personnel, droits selon le rôle,
  mots de passe chiffrés.
- **Notifications fiables** : un e-mail ne part que pour une séance réellement
  enregistrée.

## Sous le capot

Application web accessible depuis un navigateur, sans installation pour les
utilisateurs. Java 21 et Spring Boot côté serveur, React côté interface,
PostgreSQL pour les données. L'ensemble se déploie en une commande avec Docker.

---

**TutoFac** : projet du Master Informatique (P2S),
Université Sorbonne Paris Nord, 2026-2027.

Équipe : Nassim Amrouche, Yassine Berriche, Rayane Harkati, Saad Mimouni.
