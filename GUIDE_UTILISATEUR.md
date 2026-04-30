# 📘 Guide Utilisateur — EduSchedule

> Plateforme de gestion d'emplois du temps de l'**Institut Supérieur du Digital (ISD)**
> Version 1.0.0 — Avril 2026

---

## 🎯 À qui s'adresse ce guide ?

- **Secrétariat / Administration** : générer et envoyer les emplois du temps
- **Professeurs** : soumettre leurs disponibilités hebdomadaires
- **Étudiants** : consulter leur EDT par email

---

## 🔑 Accès à la plateforme

| Rôle | URL | Authentification |
|---|---|---|
| Secrétariat | https://frontend-angular-planning.vercel.app | Aucune (V1) |
| Professeur | Lien personnel reçu par email/WhatsApp | Token unique |
| Étudiant | Email reçu | Aucune action requise |

---

## 1. 👩‍💼 Côté Secrétariat — Workflow complet

### Étape 1 — Ajouter les professeurs et leurs modules

1. Menu **Professeurs** → bouton **+ Ajouter**
2. Renseigner : nom, matière, **email**, téléphone (format `+225...`, format E.164 obligatoire pour WhatsApp)
3. Menu **Modules** → bouton **+ Ajouter**
4. Associer chaque module à un professeur et un niveau (L1/L2/L3/M1/M2)

### Étape 2 — Ajouter les étudiants

1. Menu **Étudiants** → bouton **+ Ajouter**
2. Renseigner : nom, email, filière, niveau

### Étape 3 — Demander les disponibilités aux profs (lundi automatique)

**Option A — Envoi automatique (cron lundi 7h00)**
Le système envoie automatiquement chaque lundi un mail + WhatsApp à chaque prof avec un lien personnel valide 7 jours.

**Option B — Envoi manuel**
1. Menu **Tableau de bord**
2. Cliquer sur **📤 Envoyer Emails**
3. Patienter 30s → vérifier les statuts (ATTENTE → ENVOYE → REPONDU)

### Étape 4 — Suivre les réponses

1. Menu **Tableau de bord** → tableau des statuts
2. Auto-refresh toutes les 15s
3. Statut **REPONDU** = prof a soumis ses créneaux

### Étape 5 — Générer l'emploi du temps

1. Menu **Emploi du temps**
2. Cliquer sur **Générer EDT**
3. Choisir : filière, niveau, semaine
4. L'EDT est généré (créneaux triés chronologiquement)
5. Modifier les créneaux si besoin

### Étape 6 — Envoyer l'EDT aux étudiants

1. Sélectionner l'EDT généré
2. Cliquer sur **📧 Envoyer aux étudiants**
3. Le système :
   - 🤖 Optimise via Gemini IA (suggestions affichées)
   - 🎨 Génère un email HTML stylé ISD
   - 📨 Envoie aux étudiants concernés (filière + niveau matchés)

### Étape 7 — Gérer les tickets support

1. Menu **Tickets**
2. Filtrer par statut (OUVERT, EN_COURS, RESOLU)
3. Changer le statut d'un ticket en cliquant sur son badge

---

## 2. 👨‍🏫 Côté Professeur

1. Vous recevez un **email** + un **WhatsApp** chaque lundi 7h
2. Cliquez sur **« ✏️ Saisir mes disponibilités »**
3. Sur la page :
   - Ajoutez vos créneaux (jour, heure début/fin, salle, filière, niveau)
   - Validez
4. ⚠️ Le lien expire après **7 jours** — passez par la secrétaire si dépassé

---

## 3. 🎓 Côté Étudiant

1. Vous recevez un email avec votre EDT au format HTML stylé ISD
2. Pas de connexion requise
3. Pour toute question : envoyer un ticket via la plateforme (futur)

---

## 🆘 Problèmes fréquents

### « Je ne reçois pas l'email »
- Vérifier le dossier spam
- Vérifier que ton email est bien renseigné côté secrétariat
- Si Gmail bloque (`550 5.7.1`) : la secrétaire doit reconfigurer l'envoi via Resend/SendGrid

### « Le bouton Générer EDT ne fait rien »
- Vérifier qu'au moins **1 prof a répondu** pour la filière + niveau choisis
- Vérifier la console réseau (F12) pour voir les erreurs API

### « Mon WhatsApp n'arrive pas »
- Vérifier que le numéro est au format **E.164** (`+225...`)
- Twilio sandbox : il faut d'abord rejoindre le sandbox via le code de connexion

### « 500 error / IA non active »
- L'IA Gemini est optionnelle : si pas configurée, l'EDT est généré sans optimisation
- L'admin Render doit ajouter `GEMINI_API_KEY` dans les variables d'env

---

## 🔧 Pour l'admin technique

### Variables d'environnement Render

| Variable | Rôle |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC Neon |
| `SPRING_DATASOURCE_USERNAME` | User Neon |
| `SPRING_DATASOURCE_PASSWORD` | Pass Neon |
| `APP_FRONTEND_URL` | URL Vercel (pour générer les liens prof) |
| `N8N_WEBHOOK_URL` | URL n8n workflow EDT |
| `N8N_EMAIL_TRIGGER_URL` | URL n8n workflow Email |
| `GEMINI_API_KEY` | Clé Google Gemini (optionnelle) |
| `LOGGING_LEVEL_COM_SCHOOL` | DEBUG ou INFO |

### Endpoints clés

- `GET /actuator/health` — healthcheck
- `GET /api/statut` — vue d'ensemble (compteurs)
- `GET /api/ia/statut` — état de l'IA
- `POST /api/webhook/preparer-envoi` — appelé par n8n cron lundi
- `POST /api/webhook/trigger-bot` — appelé par bouton dashboard
- Documentation OpenAPI : `/swagger-ui/index.html`

---

## 📞 Support

- **Tickets** : depuis la plateforme (menu Tickets)
- **Email admin** : yohanntoure40@gmail.com
- **Repo backend** : https://github.com/yohvnn257/backend-springboot-planning
- **Repo frontend** : https://github.com/yohvnn257/frontend-angular-planning

---

*Institut Supérieur du Digital — Projet RNCP Licence 3 — Yohann Okemba — Avril 2026*
