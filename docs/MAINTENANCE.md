# Maintenance & amélioration continue — EduSchedule

> Bloc RNCP 4 : Maintenance applicative et évolutive

---

## 1. Système de gestion de tickets

### 1.1 Vue d'ensemble

Un module de tickets intégré permet à n'importe quel utilisateur (admin, prof, étudiant, support) de signaler un bug, une demande d'évolution ou une question.

**Accès** : `https://<frontend>/tickets`

### 1.2 Modèle d'un ticket

| Champ | Type | Description |
|---|---|---|
| `id` | Long | Identifiant unique |
| `titre` | String | Résumé court (obligatoire) |
| `description` | Text | Détail libre |
| `type` | Enum | `BUG`, `EVOLUTION`, `QUESTION` |
| `email` | String | Contact de l'émetteur |
| `statut` | Enum | `OUVERT`, `EN_COURS`, `RESOLU` |
| `dateCreation` | DateTime | Auto |
| `dateResolution` | DateTime | Renseigné quand statut → `RESOLU` |

### 1.3 Cycle de vie

```
[Création]
   │
   ▼
OUVERT ───► EN_COURS ───► RESOLU
   ▲           │             │
   └───────────┴─────────────┘
       (réouverture possible
        en repassant à OUVERT)
```

### 1.4 API

| Méthode | URI | Action |
|---|---|---|
| `GET` | `/api/tickets` | Liste tous les tickets |
| `GET` | `/api/tickets?statut=OUVERT` | Filtre par statut |
| `GET` | `/api/tickets/stats` | Compte total / ouverts / en cours / résolus |
| `POST` | `/api/tickets` | Crée un ticket |
| `PATCH` | `/api/tickets/{id}/statut` | Change le statut |
| `DELETE` | `/api/tickets/{id}` | Supprime un ticket |

### 1.5 Procédure de traitement (côté support)

1. **Triage quotidien** : ouvrir l'écran Tickets, filtrer sur **OUVERT**
2. **Prise en charge** : passer le ticket en **EN_COURS**, contacter l'émetteur si besoin
3. **Investigation** :
   - **BUG** → reproduire, identifier la cause, fixer dans le code, déployer
   - **EVOLUTION** → estimer, planifier, livrer
   - **QUESTION** → répondre par email à l'émetteur
4. **Clôture** : passer en **RESOLU** (la `dateResolution` est auto-renseignée)

### 1.6 SLA cible

| Type | Première réponse | Résolution cible |
|---|---|---|
| BUG bloquant | < 4h | < 24h |
| BUG non bloquant | < 24h | < 1 semaine |
| EVOLUTION | < 1 semaine | Selon roadmap |
| QUESTION | < 48h | N/A |

---

## 2. Maintenance applicative

### 2.1 Monitoring

| Indicateur | Outil | Seuil d'alerte |
|---|---|---|
| Disponibilité backend | Render Healthcheck | `/actuator/health` ≠ 200 |
| Logs erreurs | Render Logs | Stack traces récurrentes |
| Disponibilité frontend | Vercel Analytics | Build failed |
| Quota DB | Neon Dashboard | > 80% du quota Free |
| Coût Anthropic | console.anthropic.com | Pic anormal de tokens |

### 2.2 Logs

Tous les logs sont centralisés sur **Render Dashboard → Logs**.

Niveaux configurés (`application.properties`) :
- `com.school` : DEBUG (logique métier détaillée)
- `org.hibernate.SQL` : WARN (pas de pollution avec les requêtes)

**Recherche d'une erreur** :
```
Render Dashboard → Logs → Search → "ERROR" ou nom de classe Java
```

### 2.3 Sauvegardes

- **DB** : Neon PITR 7 jours (automatique)
- **Code** : Git/GitHub
- **Workflows n8n** : exportés en JSON dans `n8n-workflows/` (versionnés)
- **Variables d'environnement** : documentées dans `docs/DEPLOIEMENT.md` §3.2

### 2.4 Mises à jour de dépendances

**Cadence recommandée** : trimestrielle, sauf CVE critique.

```bash
# Backend
./gradlew.bat dependencies --configuration runtimeClasspath
# → vérifier les versions, mettre à jour build.gradle

# Frontend
cd frontend-angular-planning
npm outdated
npm update
```

**Test obligatoire après mise à jour** : la procédure de smoke test du [DEPLOIEMENT.md §8.1](DEPLOIEMENT.md#81-smoke-tests).

---

## 3. Maintenance évolutive

### 3.1 Ajouter un endpoint REST

1. Créer la méthode dans le `Service` correspondant
2. L'exposer dans le `Controller`
3. Ajouter le test (si applicable)
4. Mettre à jour la doc Swagger (auto-générée)
5. Si consommé par n8n → ajouter le nœud HTTP correspondant

### 3.2 Ajouter une migration de schéma

**Ne JAMAIS modifier une migration existante.**

```bash
# Créer un nouveau fichier
src/main/resources/db/migration/V{N+1}__description.sql

# Exemple : V6__add_color_to_module.sql
ALTER TABLE modules ADD COLUMN couleur VARCHAR(7);
```

Au prochain démarrage, Flyway l'appliquera automatiquement. **Tester d'abord en local sur une copie de la DB**.

### 3.3 Ajouter un nœud n8n

1. Ouvrir le workflow dans l'éditeur n8n
2. Ajouter le nœud
3. **Ne pas mettre de logique métier dans un nœud Code** — créer un endpoint backend à la place
4. **Save** + **Activate**
5. Exporter le JSON et commiter dans `n8n-workflows/`

### 3.4 Améliorer le prompt Claude

Le prompt est centralisé dans [`ClaudeService.java`](../src/main/java/com/school/service/ClaudeService.java).

1. Modifier la chaîne `prompt = "Tu es un expert..."`
2. Tester localement avec une vraie clé API
3. Vérifier la qualité de la réponse JSON
4. Commit + push → Render redéploie

### 3.5 Roadmap d'évolutions envisagées

| Priorité | Évolution | Bloc RNCP |
|---|---|---|
| 🟢 V1.1 | Authentification JWT (admins) | Sécurité |
| 🟢 V1.1 | Export PDF de l'EDT côté backend | UX |
| 🟡 V1.2 | Détection automatique des conflits de salle | IA |
| 🟡 V1.2 | Statistiques avancées (heures/prof, taux d'occupation) | Analytics |
| 🔵 V2 | Application mobile React Native | Mobile |
| 🔵 V2 | Multi-tenant (plusieurs établissements) | Scaling |

---

## 4. Procédures d'urgence

### 4.1 Backend down

1. Vérifier `https://backend-springboot-planning.onrender.com/actuator/health`
2. **Si timeout** → Render Dashboard → vérifier logs récents
3. **Si crash JVM** (OutOfMemory) → augmenter `JAVA_TOOL_OPTIONS=-Xmx512m` (attention quota Free)
4. **Si DB unreachable** → vérifier Neon Dashboard, status quota

### 4.2 Frontend down

1. Vérifier l'URL Vercel
2. Vercel Dashboard → Deployments → vérifier le dernier build
3. Si build failed → consulter les logs build, fixer, push

### 4.3 Bot email ne s'envoie pas

1. Tester manuellement : `POST /api/webhook/trigger-bot`
2. Vérifier les logs Render pour les erreurs
3. Vérifier que le workflow n8n est **activé**
4. Vérifier les credentials Gmail dans n8n (token expiré ?)

### 4.4 Claude IA ne répond plus

1. Vérifier `GET /api/ia/statut` → champ `actif` doit être `true`
2. Si `false` → vérifier `ANTHROPIC_API_KEY` dans Render
3. Vérifier le quota / facturation sur console.anthropic.com
4. Le système a un **fallback gracieux** : sans IA, les EDT sont quand même générés (créneaux non optimisés)

### 4.5 Migration Flyway échoue au démarrage

1. Render → Logs → repérer l'erreur SQL exacte
2. Si schéma désynchronisé : ne **pas** activer `validate-on-migrate=true`
3. Créer une migration corrective `V{N+1}__fix_X.sql`
4. Redéployer

---

## 5. Versioning et traçabilité

### 5.1 Convention de commits

```
feat:     nouvelle fonctionnalité
fix:      correction de bug
chore:    tâche technique (deps, config)
docs:     documentation seule
refactor: refactorisation sans changement fonctionnel
```

### 5.2 Tags de version

À créer manuellement pour chaque release :

```bash
git tag -a v1.0.0 -m "Première version livrable RNCP"
git push origin v1.0.0
```

### 5.3 Changelog

Maintenir un `CHANGELOG.md` à la racine pour chaque version (à initier en V1.1).

---

## 6. Contacts

| Rôle | Contact |
|---|---|
| Porteur du projet | Yohann Touré — yohanntoure40@gmail.com |
| Hébergement Render | Dashboard du compte Render |
| Hébergement Vercel | Dashboard du compte Vercel |
| Base de données Neon | console.neon.tech |
| Support Anthropic | https://support.anthropic.com |
| Support Twilio | https://www.twilio.com/help |
