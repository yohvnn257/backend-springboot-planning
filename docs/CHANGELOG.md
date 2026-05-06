# Journal des évolutions — EduSchedule

> Bloc RNCP 4 : Maintenance & amélioration continue
> Trace l'évolution du produit dans le temps. Chaque entrée correspond à un commit ou une décision architecturale.

---

## 2026-05 — Stabilisation IA & expérience utilisateur

### Mai 2026

- **Désactivation du mode `thinking` de Gemini 2.5 Flash** + raccourcissement des prompts
  → résout les erreurs `MAX_TOKENS` constatées sur les comptes free tier.
- **Fallback automatique** : si `gemini-3-flash-preview` indisponible (HTTP 503), bascule transparente sur `gemini-2.5-flash`.
- **Refonte des cas d'usage IA** (3 actions stables, plus de modification automatique des EDT) :
  1. `Conseils IA` (page EDT) : suggestions textuelles.
  2. `Analyser avec IA` (page Disponibilités) : analyse de couverture + recommandations.
  3. Intro IA personnalisée dans l'email étudiant (workflow n8n 2).
- **Détection de conflits horaires** côté backend (prof double-booké, salle double-bookée) avec affichage visuel sur la grille EDT.
- **Logo officiel ISD** intégré (SVG hébergé sur Vercel) dans email étudiant + export PDF.
- **Format date/niveau ISD** : `EMPLOI DU TEMPS (4 - 8 MAI 2026)` + `LICENCE 3 : DÉVELOPPEMENT WEB`.
- **Snap automatique Lundi → Vendredi** dans la modal "Générer un EDT".

### Avril 2026

- **Migration Anthropic Claude → Google Gemini** pour des raisons de coût (free tier `aistudio.google.com/apikey`).
- **Migration Railway → Render** pour le backend (essai Railway expiré).
- **Migration BDD vers Neon PostgreSQL** (Frankfurt) — free tier sans expiration.
- **Migration Brevo (SMTP bloqué) → Mailjet** (HTTP API) pour la livraison des emails — résout les rejets `550 5.7.1`.
- **Suppression du champ `matiere` sur `Professeur`** : un prof peut enseigner plusieurs modules → la table `modules` couvre ce besoin (V6).
- **Filières officielles ISD** importées depuis `institutdigital.com` : Développement Web / Marketing Digital / Communication Digitale / Intelligence Artificielle / Data Science.
- **Architecture LLM-as-Microservice** confirmée : tout appel à Gemini transite par Spring Boot, jamais directement depuis n8n.
- **Système de tickets** (V5) : back-office support (BUG / SUGGESTION / QUESTION).
- **CORS restreint** aux domaines Vercel (production + previews) au lieu de `*`.

---

## Avril 2026 — Initialisation du produit

- **V1** : tables `professeurs`, `etudiants`, `disponibilites`, `emplois_du_temps`.
- **V2** : données démo (3 profs, 3 étudiants).
- **V3** : token de réponse signé + table `modules`.
- **V4** : colonne `salle` sur `modules`.
- **V5** : table `tickets` (système de support).
- **V6** : DROP `matiere` de `professeurs`.
- **V7** : `filiere` et `niveau` nullables sur `disponibilites` (assignés par la secrétaire à la génération de l'EDT).

---

## Décisions techniques notables

| Date | Décision | Justification |
|---|---|---|
| 2026-04-20 | Frontend Angular 19 standalone components | Pas de NgModule, signals + OnPush |
| 2026-04-22 | Backend Spring Boot 3.4.3 + Java 21 | Stable LTS, Hibernate 6 |
| 2026-04-23 | PostgreSQL Neon (Frankfurt) | Free tier sans expiration, scale-to-zero |
| 2026-04-25 | Pattern LLM-as-Microservice | Centralisation appels IA dans le backend |
| 2026-04-28 | n8n self-hosted (localhost + ngrok) | Pas de coût supplémentaire pour la démo |
| 2026-05-02 | Mailjet HTTP API (vs node n8n) | Pas de credential n8n, header simple |
| 2026-05-04 | Bascule Gemini 3 Flash preview → 2.5 Flash | Stabilité production (modèle preview surchargé) |

---

## Backlog d'évolutions futures (post-soutenance)

- **Authentification Spring Security + JWT** sur les endpoints admin.
- **Intégration Mailtrap / domaine custom** pour la délivrabilité email production.
- **Tests d'intégration Testcontainers + PostgreSQL** (couverture cible 60%).
- **Page Statistiques** avec graphiques Chart.js (taux réponse profs, modules par filière).
- **Mode hors-ligne** (PWA) pour la consultation EDT.
- **Notifications push** au prof quand l'EDT est envoyé (Firebase Cloud Messaging).
- **Export Excel** en plus du PDF.
- **Audit log** (qui a modifié quoi, quand) pour traçabilité RNCP.
