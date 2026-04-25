# Guide de déploiement — EduSchedule

> Bloc RNCP 3 : Mise en ligne & exploitation

---

## 1. Vue d'ensemble du déploiement

| Composant | Plateforme | URL production |
|---|---|---|
| Backend Spring Boot | **Render** (Docker) | `https://backend-springboot-planning.onrender.com` |
| Frontend Angular | **Vercel** (static) | `https://<votre-projet>.vercel.app` |
| Base de données | **Neon** (PostgreSQL) | `<neon-host>:5432/school_db` |
| Workflows | **n8n** (cloud ou self-host) | `https://<n8n-host>/webhook/...` |

---

## 2. Pré-requis

- Compte **GitHub** (pour héberger le code)
- Compte **Render** (gratuit)
- Compte **Vercel** (gratuit)
- Compte **Neon** (gratuit — 0.5 Go DB)
- Compte **n8n Cloud** (essai gratuit) **ou** instance self-hosted
- Compte **Anthropic** avec clé API (ou laisser l'IA désactivée)
- Compte **Google** + activation Gmail OAuth2 dans n8n
- Compte **Twilio** + Sandbox WhatsApp activée

---

## 3. Déploiement Backend (Render)

### 3.1 Création du service

1. Push le code sur GitHub
2. Connecter le repo à Render → **New** → **Web Service** → **Docker**
3. Sélectionner le repo, branche `main`
4. Render détecte automatiquement le `Dockerfile` et le `render.yaml`
5. Région recommandée : **Frankfurt** (proche utilisateurs FR)
6. Plan : **Free**

### 3.2 Variables d'environnement

À configurer dans **Render → Settings → Environment** :

| Variable | Valeur exemple | Source |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ep-xxx.eu-central-1.aws.neon.tech/school_db?sslmode=require` | Neon Dashboard |
| `SPRING_DATASOURCE_USERNAME` | `school_user` | Neon Dashboard |
| `SPRING_DATASOURCE_PASSWORD` | `********` | Neon Dashboard |
| `ANTHROPIC_API_KEY` | `sk-ant-api03-...` | console.anthropic.com |
| `ANTHROPIC_MODEL` | `claude-sonnet-4-6` | (défaut OK) |
| `N8N_WEBHOOK_URL` | `https://<n8n-host>/webhook/emploi-du-temps` | n8n workflow 2 |
| `N8N_EMAIL_TRIGGER_URL` | `https://<n8n-host>/webhook/email-trigger` | n8n workflow 1 |
| `APP_FRONTEND_URL` | `https://eduschedule.vercel.app` | URL Vercel |
| `JAVA_TOOL_OPTIONS` | `-Xmx400m` | (défaut OK) |

### 3.3 Healthcheck & monitoring

- Healthcheck Render : `/actuator/health` (status 200 = service OK)
- Logs en temps réel : **Render Dashboard → Logs**
- Métriques : CPU / RAM / Requests visibles dans le dashboard

### 3.4 Build & déploiement

Render redéploie automatiquement à chaque push sur `main`. Le `Dockerfile` exécute :

```
FROM gradle:8.11-jdk21 → build du JAR
FROM eclipse-temurin:21-jre-alpine → runtime léger
```

Temps de build moyen : **~3 minutes**. Premier appel après 15min d'inactivité : **~30s** (cold start Free tier).

---

## 4. Déploiement Base de Données (Neon)

### 4.1 Création de l'instance

1. https://neon.tech → **Create Project**
2. Région : **eu-central-1 (Frankfurt)** pour minimiser la latence avec Render
3. Database name : `school_db`
4. Récupérer l'URL de connexion → injecter dans `SPRING_DATASOURCE_URL`

### 4.2 Migrations

Flyway s'exécute automatiquement au démarrage du backend (`spring.flyway.enabled=true`).

Les migrations dans `src/main/resources/db/migration/V*.sql` créent toutes les tables et données de démo. **Ne jamais modifier une migration déjà appliquée** — créer un nouveau `V{N+1}__*.sql`.

### 4.3 Backup

Neon Free fait des backups automatiques (point-in-time recovery 7 jours). Pour un export manuel :

```bash
pg_dump "postgresql://user:pass@ep-xxx.eu-central-1.aws.neon.tech/school_db" > backup.sql
```

---

## 5. Déploiement Frontend (Vercel)

### 5.1 Création du projet

1. https://vercel.com → **Add New** → **Project**
2. Importer le repo GitHub
3. **Root Directory** : `frontend-angular-planning`
4. Framework preset : **Angular** (auto-détecté)
5. Build Command : `npm run build:prod`
6. Output Directory : `dist/frontend-angular-planning/browser`

### 5.2 Configuration

Le `vercel.json` gère le routage SPA :

```json
{ "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }] }
```

### 5.3 Environnements

L'URL backend est dans `src/environments/environment.prod.ts` :

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://backend-springboot-planning.onrender.com/api'
};
```

Vercel build automatiquement avec `--configuration production` qui remplace `environment.ts` par `environment.prod.ts`.

---

## 6. Déploiement n8n

### 6.1 Option 1 — n8n Cloud (recommandé)

1. https://n8n.cloud → créer un compte
2. **Workflows** → **Import from File** → importer :
   - `n8n-workflows/workflow1-bot-email.json`
   - `n8n-workflows/workflow2-envoi-edt.json`

### 6.2 Option 2 — Self-host (Docker)

```bash
docker run -d --name n8n -p 5678:5678 \
  -v n8n_data:/home/node/.n8n \
  -e N8N_BASIC_AUTH_ACTIVE=true \
  -e N8N_BASIC_AUTH_USER=admin \
  -e N8N_BASIC_AUTH_PASSWORD=<password> \
  n8nio/n8n
```

Pour un accès public depuis Render, utiliser **ngrok** ou un reverse proxy.

### 6.3 Configuration des credentials

Dans **n8n → Credentials**, créer :

1. **Gmail OAuth2** (Settings → Credentials → New → Gmail OAuth2)
2. **Twilio API** (Account SID + Auth Token depuis console.twilio.com)

⚠️ **Plus besoin de credential Anthropic dans n8n** — Claude est désormais centralisé dans le backend Spring.

### 6.4 Activation et URLs

1. Activer chaque workflow (toggle en haut à droite)
2. Récupérer l'URL webhook de chaque workflow (clic sur le nœud Webhook)
3. Mettre à jour les variables Render :
   - `N8N_EMAIL_TRIGGER_URL` ← URL workflow 1
   - `N8N_WEBHOOK_URL` ← URL workflow 2

---

## 7. Procédure de déploiement type (mise à jour)

### Backend

```bash
# 1. Modifications locales
./gradlew.bat compileJava        # vérification
./gradlew.bat test               # tests

# 2. Commit & push
git add .
git commit -m "feat: nouvelle fonctionnalité"
git push origin main

# 3. Render redéploie automatiquement (~3min)
# 4. Vérifier les logs Render
```

### Frontend

```bash
cd frontend-angular-planning
npm run build:prod               # vérification build
git add .
git commit -m "feat: nouvelle fonctionnalité UI"
git push origin main

# Vercel redéploie automatiquement (~1min)
```

### n8n

1. Modifier le workflow dans l'éditeur visuel n8n
2. **Save** + **Activate**
3. Exporter le JSON et le commiter dans `n8n-workflows/` (versioning)

---

## 8. Tests post-déploiement

### 8.1 Smoke tests

```bash
# Healthcheck backend
curl https://backend-springboot-planning.onrender.com/actuator/health
# → {"status":"UP"}

# Statut global
curl https://backend-springboot-planning.onrender.com/api/statut
# → {"backend":"UP","totalProfesseurs":N, ...}

# Statut IA
curl https://backend-springboot-planning.onrender.com/api/ia/statut
# → {"actif":true,"modele":"claude-sonnet-4-6","message":"Claude IA connecté ✅"}
```

### 8.2 Test bout-en-bout

1. Ouvrir le frontend Vercel
2. Dashboard → cliquer **"Envoyer Emails"**
3. Vérifier qu'un email arrive sur la boîte d'un prof de test
4. Cliquer le lien dans l'email → arriver sur `/repondre/<token>` du frontend
5. Saisir des créneaux → soumettre
6. Retourner au dashboard → le prof doit passer en statut **"Répondu"**
7. Aller sur **Emploi du temps** → générer un EDT pour la filière/niveau du prof
8. Cliquer **"Envoyer aux étudiants"** → vérifier qu'un email HTML arrive

---

## 9. Rollback

### Backend

Render garde les builds précédents. Pour rollback :
**Render Dashboard → Deploys → cliquer le déploiement précédent → Redeploy**

### Frontend

Vercel garde tous les déploiements. Pour rollback :
**Vercel Dashboard → Deployments → cliquer un ancien → Promote to Production**

### Base de données

⚠️ **Pas de rollback automatique des migrations Flyway**. Pour annuler une migration : créer une nouvelle migration `V{N+1}__rollback_X.sql` qui inverse les changements.

---

## 10. Coûts mensuels estimés

| Service | Plan | Coût |
|---|---|---|
| Render | Free | 0 € |
| Vercel | Hobby | 0 € |
| Neon | Free (0.5 Go) | 0 € |
| n8n Cloud | Trial puis Starter | 0–20 € |
| Anthropic | Pay-per-use | ~1–5 € (faible volume) |
| Twilio | Sandbox WhatsApp | 0 € (limites de test) |
| **Total** | | **~0–25 €/mois** |
