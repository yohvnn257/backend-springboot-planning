# Architecture technique — EduSchedule

> Bloc RNCP 2 : Conception & production

---

## 1. Vision d'ensemble

EduSchedule est une application **3-tiers + orchestrateur NoCode**, conçue selon le pattern **LLM-as-Microservice** :

```
┌──────────────────────────────────────────────────────────────────┐
│                     UTILISATEURS                                 │
│   Admin planning · Professeurs · Étudiants · Support             │
└─────────────────────┬───────────────────────────┬────────────────┘
                      │                           │
                      ▼                           ▼
              ┌───────────────┐          ┌────────────────────┐
              │  Frontend     │          │  Email / WhatsApp  │
              │  Angular 19   │          │  (côté utilisateur)│
              │  (Vercel)     │          └────────▲───────────┘
              └───────┬───────┘                   │
                      │ HTTPS REST                │
                      ▼                           │
              ┌──────────────────────────────────┴─────────────┐
              │   Backend Spring Boot 3.4 (Render — Docker)    │
              │   ┌──────────────────────────────────────────┐ │
              │   │ Controllers REST                         │ │
              │   │ Services métier (EmailService,           │ │
              │   │   ClaudeService, EmploiDuTempsService…)  │ │
              │   │ Repositories JPA                         │ │
              │   └────────┬──────────────────┬──────────────┘ │
              └────────────┼──────────────────┼────────────────┘
                           │                  │
                ┌──────────▼─────┐   ┌────────▼─────────────┐
                │  PostgreSQL    │   │  Anthropic Claude    │
                │  (Neon)        │   │  Sonnet 4.6          │
                └────────────────┘   └──────────────────────┘
                           ▲
                           │ HTTP REST
                           │
              ┌────────────┴──────────────┐
              │  n8n (orchestrateur)      │
              │  ├ Workflow 1 — bot email │
              │  └ Workflow 2 — envoi EDT │
              │   Cron · Gmail · Twilio   │
              └───────────────────────────┘
```

---

## 2. Choix d'architecture clés

### 2.1 Pattern LLM-as-Microservice

**Décision** : Claude est encapsulé dans le backend Spring (`ClaudeService`), exposé via REST (`/api/ia/*`). Ni le frontend ni n8n n'appellent l'API Anthropic directement.

**Justification** (cf. recherche [LLM-as-Microservice](https://shukriev.medium.com/llm-as-microservice-integration-patterns-and-trade-offs-f05b29945489), [AI Gateway pattern MLflow](https://mlflow.org/ai-gateway)) :
- Une seule clé API à gérer (uniquement dans Render)
- Un seul prompt à versionner (dans Git)
- Logique de fallback centralisée si l'IA est indisponible
- Logs unifiés dans Render
- Testable en JUnit

**Alternative écartée** : nœud "AI Agent" de n8n. Pertinent pour des workflows agentic (tool-calling autonome), inutile ici car nos appels Claude sont déterministes (input → output JSON).

### 2.2 n8n comme orchestrateur dumb

**Décision** : n8n ne contient aucune logique métier. Ses workflows ne font que :
- Déclencher (cron, webhook)
- Appeler les endpoints REST du backend
- Formater les emails HTML
- Router vers Gmail / Twilio

**Justification** : tout changement métier passe par Git/Spring (versionné, testable). n8n reste une couche visuelle d'orchestration que le jury RNCP peut comprendre en 30 secondes.

### 2.3 Migrations Flyway

**Décision** : Flyway pilote l'évolution du schéma PostgreSQL. JPA (`hibernate.ddl-auto=validate`) ne fait que valider la cohérence à chaud.

**Justification** : reproductibilité des environnements (local, Render), historique versionné, rollback possible.

### 2.4 Pas d'authentification (V1)

**Décision** : aucune authentification utilisateur dans la V1 — accès interne via tokens uniques pour les professeurs.

**Justification** : périmètre RNCP couvert sans complexification ; les professeurs accèdent à leur formulaire via un lien à token UUID + expiration 7 jours, ce qui sécurise sans introduire d'auth.

---

## 3. Modèle de données

### 3.1 Entités principales

```
┌──────────────┐         ┌──────────────────┐
│ Professeur   │ 1     N │ Disponibilite    │
│──────────────│─────────│──────────────────│
│ id           │         │ id               │
│ nom          │         │ professeur_id FK │
│ matiere      │         │ filiere          │
│ email UQ     │         │ niveau           │
│ telephone    │         │ jour DATE        │
│ whatsappStatut│        │ heureDebut TIME  │
│ responseToken│         │ heureFin TIME    │
│ tokenExpireAt│         │ salle            │
└──────┬───────┘         │ statut           │
       │                 └──────────────────┘
       │ 1
       │
       │ N
┌──────▼───────┐         ┌──────────────────┐
│ Module       │         │ Etudiant         │
│──────────────│         │──────────────────│
│ id           │         │ id               │
│ nom          │         │ nom              │
│ professeur_id│         │ email UQ         │
│ filiere      │         │ filiere          │
│ niveau       │         │ niveau           │
│ salle        │         └──────────────────┘
└──────────────┘

┌────────────────┐         ┌──────────────┐
│ EmploiDuTemps  │         │ Ticket       │
│────────────────│         │──────────────│
│ id             │         │ id           │
│ filiere        │         │ titre        │
│ niveau         │         │ description  │
│ semaineDu      │         │ type         │
│ semaineAu      │         │ email        │
│ creneauxJson   │         │ statut       │
│ statut         │         │ dateCreation │
│ dateCreation   │         │ dateResolution│
└────────────────┘         └──────────────┘
```

### 3.2 Migrations Flyway

| Version | Fichier | Contenu |
|---|---|---|
| V1 | `V1__create_tables.sql` | Tables principales (Professeur, Disponibilite, EmploiDuTemps, Etudiant) |
| V2 | `V2__insert_demo_data.sql` | Jeu de données de démonstration |
| V3 | `V3__add_modules_and_token.sql` | Table Module + colonnes token + telephone |
| V4 | `V4__add_salle_to_modules.sql` | Colonne `salle` sur Module |
| V5 | `V5__add_tickets.sql` | Table Ticket pour le système de support |

---

## 4. Backend Spring Boot

### 4.1 Structure des packages

```
com.school
├── SchoolApplication.java
├── config/
│   ├── AppConfig.java            (RestTemplate, ObjectMapper)
│   └── CorsConfig.java
├── controller/                    (couche REST)
│   ├── DisponibiliteController, EmploiDuTempsController,
│   │   EtudiantController, ModuleController, ProfesseurController,
│   │   ReponseController, StatutController, TicketController,
│   │   IaController, WebhookController
├── dto/
│   ├── request/                   (ProfesseurRequest, EtudiantRequest, etc.)
│   └── response/                  (ApiResponse)
├── entity/                        (JPA — Professeur, Etudiant, Module, etc.)
├── exception/
│   ├── GlobalExceptionHandler.java (capture + mappe en HTTP)
│   └── ResourceNotFoundException.java
├── repository/                    (Spring Data JPA)
└── service/                       (logique métier)
    ├── ClaudeService.java         (appels Anthropic API)
    ├── EmailService.java          (bot email + tokens)
    ├── EmploiDuTempsService.java  (génération + envoi EDT)
    └── ProfesseurService, ModuleService, EtudiantService, DisponibiliteService
```

### 4.2 Endpoints REST principaux

| Méthode | URI | Rôle |
|---|---|---|
| GET | `/api/statut` | Stats globales + flag IA active |
| GET | `/api/professeurs` | Liste des professeurs |
| POST | `/api/professeurs` | Création professeur |
| PATCH | `/api/professeurs/{id}/whatsapp-statut` | Mise à jour statut bot (appelé par n8n) |
| GET | `/api/disponibilites` | Liste des créneaux |
| POST | `/api/emplois-du-temps` | Génère un EDT |
| POST | `/api/emplois-du-temps/{id}/envoyer` | Déclenche n8n workflow 2 |
| POST | `/api/webhook/preparer-envoi` | Génère tokens + retourne profs (appelé par n8n) |
| POST | `/api/webhook/trigger-bot` | Déclenche n8n workflow 1 (depuis frontend) |
| POST | `/api/ia/optimiser-edt` | Claude optimise un EDT |
| POST | `/api/ia/analyser-disponibilites` | Claude analyse les disponibilités |
| GET | `/api/tickets` | Liste tickets (filtre par statut) |
| POST | `/api/tickets` | Création ticket |

Documentation interactive : [Swagger UI](http://localhost:8080/swagger-ui.html).

---

## 5. Frontend Angular

### 5.1 Modules métier

```
src/app/modules/eduschedule/
├── dashboard/        (vue d'ensemble + bot email)
├── professeurs/      (CRUD professeurs)
├── modules/          (CRUD modules d'enseignement)
├── etudiants/        (CRUD étudiants)
├── disponibilites/   (consultation des créneaux)
├── emploi-du-temps/  (génération + envoi EDT)
├── repondre/         (formulaire public à token pour professeurs)
├── ia/               (dashboard Claude IA)
└── tickets/          (système de support)
```

### 5.2 Choix techniques Angular

- **Standalone components** (pas de NgModule) — alignés sur les bonnes pratiques Angular 17+
- **Signals** pour la gestion de l'état réactif
- **OnPush** par défaut pour les performances
- **PrimeNG 19** pour les composants UI (tables, dialogs, boutons)
- **HttpClient** + interceptor d'erreurs global

---

## 6. Workflows n8n

### 6.1 Workflow 1 — Bot Email + WhatsApp

```
Cron (Lundi 7h)              Webhook /email-trigger
        │                            │
        └──────────────┬─────────────┘
                       ▼
        POST /api/webhook/preparer-envoi (Render)
                       │
                       ▼
            Split par professeur
                  │      │
        ┌─────────┘      └─────────┐
        ▼                          ▼
   Gmail (Email HTML)       Twilio WhatsApp
        │
        ▼
   PATCH /api/professeurs/{id}/whatsapp-statut
```

### 6.2 Workflow 2 — Envoi EDT

```
Webhook /emploi-du-temps
        │
        ▼
   Préparer données (Set)
        │
        ▼
   POST /api/ia/optimiser-edt (Claude via backend)
        │
        ▼
   Fusion réponse IA (Set)
        │
        ▼
   Générer HTML — Style ISD (Code)
        │
        ▼
   Split par destinataire
        │
        ▼
   Gmail (envoi à chaque étudiant)
```

---

## 7. Gestion des erreurs

### 7.1 Backend

- `GlobalExceptionHandler` capture toutes les exceptions et renvoie un JSON normalisé `{erreur: "..."}`
- `ResourceNotFoundException` → 404
- `IllegalArgumentException` → 400
- Toute autre exception → 500 + log

### 7.2 IA Claude

- Si `ANTHROPIC_API_KEY` est absente → fallback : retourne les créneaux non optimisés + message explicite, `iaActive: false`
- Si erreur réseau Anthropic → fallback identique
- Le frontend affiche dynamiquement un bandeau "✅ Connecté" ou "⚠️ Clé API manquante"

### 7.3 n8n

- Timeout HTTP 60s sur les appels backend (cold start Render)
- Si Twilio échoue, le PATCH statut ne se déclenche que depuis le flow Email (Twilio est best-effort)

---

## 8. Sécurité

| Risque | Mitigation |
|---|---|
| Accès non autorisé au formulaire prof | Token UUID 32 caractères + expiration 7 jours |
| Injection SQL | JPA paramétré, pas de requêtes natives concaténées |
| CORS | Configuration explicite via `CorsConfig` (whitelist du domaine Vercel) |
| Secrets en dur | Tous les secrets via variables d'environnement Render |
| XSS dans les emails | Variables échappées par n8n templating |

---

## 9. Performance & scalabilité

- **Cold start Render Free** : ~30s — acceptable pour un usage interne hebdomadaire
- **Heap JVM** : limité à 400 Mo (`JAVA_TOOL_OPTIONS=-Xmx400m`) pour rester dans le quota Render Free
- **Hibernate** : `open-in-view=false` pour éviter les requêtes N+1
- **Frontend** : lazy-loading des routes via `loadComponent`

---

## 10. Décisions documentées

| Décision | Date | Raison |
|---|---|---|
| Switch Railway → Render | 2026-04-21 | Plan free plus généreux, healthcheck natif |
| Switch DB locale → Neon | 2026-04-22 | Persistence cloud, pas de DB locale en prod |
| Claude Sonnet 4.5 → 4.6 | 2026-04-23 | Modèle plus récent, meilleur ratio coût/qualité |
| Centralisation Claude dans Spring | 2026-04-24 | Pattern LLM-as-Microservice (cf. §2.1) |
