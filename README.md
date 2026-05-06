# EduSchedule — Institut Supérieur du Digital

Plateforme de gestion intelligente d'emplois du temps universitaires, propulsée par Gemini IA et n8n.

> **Projet de diplomation RNCP Licence 3** — Institut Supérieur du Digital.

---

## Sommaire

- [Vue d'ensemble](#vue-densemble)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Démarrage rapide](#démarrage-rapide)
- [Documentation](#documentation)
- [Liens production](#liens-production)

---

## Vue d'ensemble

EduSchedule automatise le cycle complet de planification scolaire :

1. **Collecte des disponibilités** — un bot envoie chaque lundi à 7h un email + WhatsApp à chaque professeur avec un lien sécurisé pour saisir ses créneaux.
2. **Optimisation par IA** — Gemini (Google) analyse les disponibilités et génère un emploi du temps équilibré (max 2 cours/jour, préférence matinale).
3. **Diffusion** — l'EDT optimisé est envoyé par email aux étudiants concernés au format HTML imprimable.
4. **Support utilisateur** — un système de tickets intégré permet aux utilisateurs de signaler bugs et demandes d'évolution.

---

## Stack technique

| Couche | Technologie | Hébergement |
|---|---|---|
| **Frontend** | Angular 19 + PrimeNG | Vercel |
| **Backend** | Spring Boot 3.4 (Java 21) | Render (Docker) |
| **Base de données** | PostgreSQL | Neon |
| **Orchestration NoCode** | n8n | n8n Cloud / self-host |
| **IA** | Gemini 2.5 Flash (Google) | API |
| **Notifications** | Gmail (n8n) + Twilio WhatsApp | API |

---

## Architecture

```
Frontend Angular (Vercel)
      │ HTTPS
      ▼
Backend Spring Boot (Render)  ◄──── single source of truth ────►  PostgreSQL (Neon)
      │              ▲
      │              │ HTTPS
      ▼              │
   Gemini API   ◄────┘
                     │
n8n (orchestrateur)  │
   ├─ Workflow 1 : bot email + WhatsApp (cron lundi 7h)
   └─ Workflow 2 : envoi EDT optimisé aux étudiants
```

**Principe** : Spring Boot porte toute la logique métier (DB, Gemini, validation). n8n n'est qu'un orchestrateur visuel — il appelle les endpoints REST du backend. Cette séparation respecte le pattern *LLM-as-Microservice* et garantit une seule source de vérité.

→ Voir [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) pour le détail.

---

## Démarrage rapide

### Prérequis

- Java 21+
- Node.js 20+
- PostgreSQL 14+ (ou compte Neon)
- (Optionnel) Compte n8n + Twilio + Google pour les fonctionnalités complètes

### Backend

```bash
./gradlew bootRun
```

Variables d'environnement minimales (sinon valeurs par défaut dans `application.properties`) :

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/school_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
GEMINI_API_KEY=sk-ant-...        # optionnel — sans clé, l'IA est désactivée proprement
N8N_WEBHOOK_URL=...                 # workflow 2 (envoi EDT)
N8N_EMAIL_TRIGGER_URL=...           # workflow 1 (bot email)
APP_FRONTEND_URL=http://localhost:4200
```

API disponible sur `http://localhost:8080/api`. Doc Swagger : `http://localhost:8080/swagger-ui.html`.

### Frontend

```bash
cd frontend-angular-planning
npm install
npm start
```

Application accessible sur `http://localhost:4200`.

### n8n

1. Importer les deux workflows depuis `n8n-workflows/` :
   - `workflow1-bot-email.json`
   - `workflow2-envoi-edt.json`
2. Configurer les credentials Gmail OAuth2 + Twilio
3. Récupérer les URLs webhook produites et les mettre dans les variables d'environnement Render

---

## Documentation

| Bloc RNCP | Document | Description |
|---|---|---|
| 1. Cadrage | [docs/CAHIER_DES_CHARGES.md](docs/CAHIER_DES_CHARGES.md) | Objectifs, périmètre, livrables, processus métier |
| 2. Build | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Conception technique, choix d'architecture, modèle de données |
| 3. Live | [docs/DEPLOIEMENT.md](docs/DEPLOIEMENT.md) | Mise en production, CI, monitoring |
| 4. Maintenance | [docs/MAINTENANCE.md](docs/MAINTENANCE.md) | Système de tickets, procédures, évolutions |
| 5. Onboarding | [docs/GUIDE_UTILISATEUR.md](docs/GUIDE_UTILISATEUR.md) | Tutoriel pas-à-pas pour les utilisateurs finaux |
| Démo | [docs/SCRIPT_DEMO_VIDEO.md](docs/SCRIPT_DEMO_VIDEO.md) | Script de la vidéo de présentation |
| 🎥 **Vidéo de démonstration** | **[VIDEO_DEMO_LINK_HERE](VIDEO_DEMO_LINK_HERE)** *(remplacer par lien Loom après tournage)* | Démo complète 6 minutes |
| Évolutions | [docs/CHANGELOG.md](docs/CHANGELOG.md) | Journal des améliorations |

---

## Liens production

- **Frontend** : déployé sur Vercel
- **Backend** : `https://backend-springboot-planning.onrender.com`
- **Healthcheck** : `https://backend-springboot-planning.onrender.com/actuator/health`
- **API docs** : `https://backend-springboot-planning.onrender.com/swagger-ui.html`

---

## Licence

Projet académique — Institut Supérieur du Digital, 2026.
