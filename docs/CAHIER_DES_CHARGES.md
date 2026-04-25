# Cahier des charges — EduSchedule

> Bloc RNCP 1 : Cadrage & structuration du projet

**Version :** 1.0
**Date :** 2026-04-24
**Porteur :** Yohann Touré — Licence 3 Institut Supérieur du Digital

---

## 1. Contexte et problématique

### 1.1 Situation actuelle

À l'Institut Supérieur du Digital, la planification hebdomadaire des cours repose sur un processus manuel chronophage :

1. Le service planning collecte les disponibilités des professeurs **par téléphone, email ou en présentiel** — souvent avec relances multiples.
2. Les disponibilités sont **consolidées dans un tableur Excel**.
3. Un humain doit ensuite **assembler manuellement** les créneaux pour produire un emploi du temps qui respecte les contraintes (un prof ne peut pas être à deux endroits à la fois, équilibrage de la semaine, capacité des salles).
4. L'emploi du temps est **diffusé par email** aux étudiants, parfois imprimé.

**Temps moyen mesuré : 6h par semaine** pour le service planning, avec un risque d'erreur élevé (oublis, conflits de salles, créneaux contradictoires).

### 1.2 Problématique

> Comment **automatiser intégralement** le cycle de planification hebdomadaire tout en garantissant un emploi du temps **optimisé** (équilibré, sans conflit) et **diffusé en temps réel** aux étudiants ?

---

## 2. Objectifs

### 2.1 Objectifs opérationnels

| # | Objectif | Indicateur de succès |
|---|---|---|
| O1 | Réduire le temps de collecte des disponibilités | < 1h / semaine (vs 6h aujourd'hui) |
| O2 | Garantir 100% de réponses des professeurs | Système de relance automatique |
| O3 | Produire un EDT optimisé sans intervention humaine | EDT généré en < 30 secondes |
| O4 | Diffuser l'EDT le jour même aux étudiants concernés | Email envoyé en < 1 minute après génération |
| O5 | Permettre aux utilisateurs de signaler bugs / demandes | Interface de tickets accessible 24/7 |

### 2.2 Objectifs RNCP couverts

| Bloc RNCP | Couverture dans le projet |
|---|---|
| 1. Cadrage & structuration | Ce document + [README.md](../README.md) |
| 2. Conception & production | Code Spring + Angular + workflows n8n |
| 3. Mise en ligne & exploitation | Backend Render, Frontend Vercel, DB Neon |
| 4. Maintenance & amélioration continue | [Système de tickets](MAINTENANCE.md), monitoring Render |
| 5. Onboarding & UX | [Guide utilisateur](GUIDE_UTILISATEUR.md), vidéo démo |

---

## 3. Périmètre

### 3.1 Inclus dans le projet

- Backend REST sécurisé pour la gestion des données
- Interface web responsive (dashboard + écrans CRUD)
- Bot automatisé de collecte des disponibilités (email + WhatsApp)
- Optimisation des emplois du temps par IA
- Diffusion automatique des EDT aux étudiants
- Système de gestion des tickets utilisateurs
- Documentation technique et utilisateur complète

### 3.2 Exclus du périmètre (V1)

- Application mobile native (le web responsive suffit)
- Authentification utilisateur (V2 — actuellement accès interne)
- Gestion des absences étudiantes
- Génération de bulletins de notes
- Multi-tenant (un seul établissement)

---

## 4. Acteurs et rôles

| Acteur | Description | Interactions principales |
|---|---|---|
| **Administrateur planning** | Gère professeurs, modules, étudiants. Lance les générations d'EDT. | Dashboard web complet |
| **Professeur** | Reçoit l'email/WhatsApp de demande de disponibilités, saisit ses créneaux | Page publique sécurisée par token |
| **Étudiant** | Reçoit l'EDT par email | Email HTML lisible / imprimable |
| **Service support** | Traite les tickets utilisateurs | Module Tickets |

---

## 5. Processus métier (modélisation)

### 5.1 Processus 1 — Collecte hebdomadaire des disponibilités

```
[Lundi 7h00 : Cron n8n]
       │
       ▼
[Backend Spring : génération token unique par prof]
       │
       ├──► [n8n : envoi email Gmail à chaque prof]
       │         └─► [Prof clique sur le lien]
       │                  └─► [Frontend : saisie créneaux]
       │                           └─► [Backend : POST /api/webhook/disponibilite-whatsapp]
       │                                    └─► [DB : enregistrement + statut REPONDU]
       │
       └──► [n8n : envoi WhatsApp Twilio en parallèle]
```

### 5.2 Processus 2 — Génération et diffusion d'un EDT

```
[Admin clique "Générer un EDT" dans le frontend]
       │
       ▼
[Backend : POST /api/emplois-du-temps]
       │
       ▼
[Service : récupération disponibilités filiérées]
       │
       ▼
[DB : création EmploiDuTemps statut=GENERE]
       │
       ▼
[Admin clique "Envoyer aux étudiants"]
       │
       ▼
[Backend : POST n8n workflow 2 avec créneaux + emails étudiants]
       │
       ▼
[n8n : POST /api/ia/optimiser-edt → Claude optimise]
       │
       ▼
[n8n : génération HTML stylé + envoi Gmail à chaque étudiant]
       │
       ▼
[DB : statut EDT = ENVOYE]
```

### 5.3 Processus 3 — Cycle de vie d'un ticket support

```
[Utilisateur crée un ticket : titre + description + type]
       │
       ▼ statut = OUVERT
[Service support traite : passe à EN_COURS]
       │
       ▼
[Résolution : statut = RESOLU + dateResolution = now()]
```

---

## 6. Livrables

| # | Livrable | État |
|---|---|---|
| L1 | Code source backend Spring Boot (Git) | ✅ Livré |
| L2 | Code source frontend Angular (Git) | ✅ Livré |
| L3 | 2 workflows n8n (JSON exportés) | ✅ Livré dans `n8n-workflows/` |
| L4 | Migrations Flyway versionnées | ✅ V1 à V5 |
| L5 | Cahier des charges (ce document) | ✅ Livré |
| L6 | Documentation d'architecture | ✅ `docs/ARCHITECTURE.md` |
| L7 | Guide d'installation/déploiement | ✅ `docs/DEPLOIEMENT.md` |
| L8 | Guide utilisateur avec captures | ✅ `docs/GUIDE_UTILISATEUR.md` |
| L9 | Procédures de maintenance | ✅ `docs/MAINTENANCE.md` |
| L10 | Application en ligne fonctionnelle | ✅ Render + Vercel |
| L11 | Système de tickets opérationnel | ✅ Module Tickets |
| L12 | Vidéo de démonstration | 🎬 Script dans `docs/SCRIPT_DEMO_VIDEO.md` |

---

## 7. Organisation projet

### 7.1 Équipe et rôles

Projet **mono-développeur** (contexte académique). Le porteur assume :
- Architecture & conception
- Développement backend
- Développement frontend
- Mise en place des workflows n8n
- Déploiement & DevOps
- Documentation
- Tests

### 7.2 Méthodologie

- **Approche itérative** : développement par feature complète (vertical slice)
- **Versioning** : Git, branche unique `main` (mono-dev)
- **Commits conventionnels** : `feat:`, `fix:`, `chore:`, `docs:`
- **Outil d'assistance** : Claude Code (Anthropic) pour le pair-programming

### 7.3 Planning macroscopique

| Phase | Période | Livrables |
|---|---|---|
| Cadrage | Avril 2026 | Ce document, modélisation processus |
| Conception | Avril 2026 | Architecture, schéma DB, choix de stack |
| Développement | Avril 2026 | Backend, frontend, workflows n8n |
| Déploiement | Avril 2026 | Render + Vercel + Neon en production |
| Documentation | Avril 2026 | README, guides, vidéo |
| **Soutenance** | **30 avril 2026** | **Dépôt final RNCP** |

---

## 8. Contraintes

### 8.1 Contraintes techniques

- Hébergement gratuit obligatoire (budget étudiant) → Render Free, Vercel Free, Neon Free.
- Le plan free Render fait dormir le backend après 15 min d'inactivité (cold start ~30s acceptable).
- Quota Anthropic limité → fallback sans IA si clé absente ou erreur.

### 8.2 Contraintes RNCP

- Doit utiliser au moins **un outil NoCode** → n8n.
- Doit intégrer une **dimension IA** → Claude Sonnet 4.6.
- Doit avoir un **système de tickets** → module dédié implémenté.
- Doit être **accessible en ligne** → Render + Vercel.
- Doit fournir une **documentation utilisateur claire** → guide + vidéo.

---

## 9. Critères d'acceptation

Le projet sera considéré comme livré et conforme si :

- [x] Le backend répond sur l'URL Render avec status 200 sur `/actuator/health`
- [x] Le frontend est accessible publiquement sur Vercel
- [x] Un EDT peut être généré de bout en bout depuis le dashboard
- [x] Un email est effectivement envoyé à un professeur de test via le bot
- [x] Une notification WhatsApp est envoyée via Twilio Sandbox
- [x] Claude IA optimise l'EDT (visible dans le bandeau "🤖 Optimisé par Claude IA")
- [x] Un ticket peut être créé et passer du statut OUVERT à RESOLU
- [x] Toute la documentation listée en §6 est présente et à jour
