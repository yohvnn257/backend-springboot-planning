# CLAUDE.md — Contexte projet pour Claude Code

> Ce fichier est lu automatiquement par Claude Code à chaque session pour contextualiser ses réponses.

## Identité du projet

**EduSchedule** — projet de diplomation RNCP Licence 3 (Institut Supérieur du Digital).
Plateforme de gestion d'emplois du temps universitaires avec IA et orchestration NoCode.

**Deadline RNCP : 2026-04-30 18:00** (échéance ferme).

## Stack

- Backend : Spring Boot 3.4 + Java 21 + Gradle, déployé sur Render (Docker)
- Frontend : Angular 19 + PrimeNG, déployé sur Vercel
- DB : PostgreSQL hébergé sur Neon, migrations Flyway
- Orchestration : n8n (workflows dans `n8n-workflows/`)
- IA : Claude Sonnet 4.6 via API Anthropic
- Notifications : Gmail (via n8n) + Twilio WhatsApp (via n8n)

## Architecture clé

**Pattern *LLM-as-Microservice*** : Claude est appelé **uniquement** depuis le backend (`ClaudeService`), jamais depuis n8n directement.

```
Angular ──HTTPS──► Spring Boot ──► Claude API
                       ▲                  ▲
                       │ HTTPS            │ HTTPS
                       └─── n8n ──────────┘
```

- **Spring** = source de vérité unique : DB, logique métier, appels Claude, validation.
- **n8n** = orchestrateur visuel : cron, déclencheurs, formatage HTML email, routing Gmail/Twilio. Appelle uniquement les endpoints REST du backend.
- **Frontend** = consommateur d'API REST.

→ Détails : `docs/ARCHITECTURE.md`. Décision archivée : voir mémoire `project_architecture.md`.

## Conventions de code

- **Java** : code dense (style fichier unique court) volontaire — ne pas refactor pour "ajouter de la lisibilité". Lombok `@Getter @Setter @RequiredArgsConstructor` partout.
- **Angular** : composants standalone, signals, OnPush. Pas de NgModule.
- **DB** : migrations Flyway dans `src/main/resources/db/migration/V*.sql`. Ne jamais modifier une migration déjà appliquée — créer un nouveau `V{N+1}__*.sql`.
- **n8n** : ne jamais ajouter de logique métier dans un nœud Code — créer un endpoint Spring à la place.

## Variables d'environnement (Render)

| Variable | Usage |
|---|---|
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | PostgreSQL Neon |
| `ANTHROPIC_API_KEY` | Clé Claude (sans → IA désactivée proprement, pas d'erreur) |
| `ANTHROPIC_MODEL` | Default `claude-sonnet-4-6` |
| `N8N_WEBHOOK_URL` | URL workflow 2 (envoi EDT) |
| `N8N_EMAIL_TRIGGER_URL` | URL workflow 1 (bot email) |
| `APP_FRONTEND_URL` | URL Vercel pour générer les liens de réponse |

## Endpoints critiques

- `POST /api/webhook/preparer-envoi` — appelé par n8n workflow 1, génère tokens + retourne profs enrichis
- `POST /api/ia/optimiser-edt` — appelé par n8n workflow 2 ET le frontend, optimisation Claude
- `POST /api/ia/analyser-disponibilites` — appelé par le frontend (dashboard IA)
- `POST /api/webhook/trigger-bot` — appelé par le frontend bouton "Envoyer Emails"
- `GET /actuator/health` — healthcheck Render

## Commandes courantes

```bash
# Backend
./gradlew.bat bootRun           # Dev local
./gradlew.bat compileJava       # Vérification compilation
./gradlew.bat test              # Tests
./gradlew.bat bootJar           # Build production

# Frontend
cd frontend-angular-planning && npm start          # Dev sur :4200
cd frontend-angular-planning && npm run build:prod # Build production
```

## Pièges à éviter

- **`Map.of` est limité à 10 paires** — utiliser `Map.ofEntries()` au-delà.
- **Ne pas appeler Anthropic directement depuis n8n** — toujours passer par `/api/ia/*`.
- **Ne pas stocker la clé Anthropic dans n8n** — uniquement dans Render.
- **Twilio WhatsApp** : le numéro doit être au format E.164 (`+225...`, `+33...`). Sinon Twilio refuse.
- **`spring.flyway.validate-on-migrate=false`** est intentionnel — ne pas remettre `true` en production sans audit complet des migrations.
- **`spring.jpa.hibernate.ddl-auto=validate`** — Flyway pilote le schéma, JPA ne fait que valider.

## Système de tickets

Le bloc RNCP "Maintenance" exige un système de tickets — implémenté côté backend (`TicketController`, entité `Ticket`) et frontend (`tickets.component.ts`). Statuts : `OUVERT`, `EN_COURS`, `RESOLU`. Types : `BUG`, `EVOLUTION`, `QUESTION`.
