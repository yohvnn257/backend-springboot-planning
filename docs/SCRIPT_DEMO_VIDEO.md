# Script de la vidéo de démonstration — EduSchedule

> Vidéo recommandée par le RNCP (bloc 5 : Onboarding & UX)

**Durée cible :** 5 à 7 minutes
**Format :** Capture d'écran commentée (Loom, OBS, ou QuickTime)
**Résolution :** 1920×1080 minimum

---

## 🎬 Pré-requis avant enregistrement

- [ ] Backend Render réveillé (faire un appel à `/api/statut` 1 minute avant)
- [ ] Frontend Vercel ouvert dans un onglet propre (mode privé idéal)
- [ ] n8n ouvert dans un autre onglet (pour montrer les workflows)
- [ ] Boîte mail Gmail de test ouverte (pour montrer les emails reçus)
- [ ] WhatsApp Web ouvert (pour montrer la notification Twilio si possible)
- [ ] Données de démo en base : au moins 3 profs, 4 modules, 5 étudiants
- [ ] Micro testé (audio clair sans bruit de fond)
- [ ] Notifications système coupées (Discord, Slack, mails…)

---

## 🎤 Script détaillé

### Séquence 1 — Introduction (30 sec)

**À l'écran** : page d'accueil du frontend (Dashboard)

> *"Bonjour, je vous présente EduSchedule, une plateforme de gestion intelligente d'emplois du temps que j'ai développée dans le cadre de ma diplomation Licence 3 à l'Institut Supérieur du Digital. EduSchedule automatise tout le cycle de planification, de la collecte des disponibilités des professeurs jusqu'à la diffusion de l'EDT optimisé aux étudiants — le tout en moins d'une minute par semaine."*

### Séquence 2 — Architecture (45 sec)

**À l'écran** : ouvrir [docs/ARCHITECTURE.md](ARCHITECTURE.md) et montrer le schéma

> *"Côté technique, EduSchedule combine quatre briques : un backend Spring Boot en Java 21 hébergé sur Render, un frontend Angular 19 sur Vercel, une base PostgreSQL chez Neon, et n8n comme orchestrateur visuel. Le pattern central est 'LLM-as-Microservice' : Claude IA d'Anthropic est encapsulé dans le backend, ce qui garantit une seule clé API, un seul prompt versionné, et des logs unifiés."*

### Séquence 3 — Configuration des données (45 sec)

**À l'écran** : naviguer Professeurs → Modules → Étudiants

> *"L'admin commence par enregistrer les professeurs avec leur email et numéro WhatsApp, les modules d'enseignement, et les étudiants regroupés par filière et niveau. Tout cela se fait via une interface CRUD classique en PrimeNG."*

### Séquence 4 — Bot email + WhatsApp (1 min 30)

**À l'écran** : Dashboard → cliquer "Envoyer Emails"

> *"Le cœur du système, c'est ce bouton. Quand je clique, le backend Spring génère un token unique pour chaque professeur, puis appelle le workflow n8n via une URL webhook."*

**Basculer sur l'onglet n8n** : montrer le workflow 1 en cours d'exécution

> *"Voici le workflow n8n. Il reçoit la liste des profs avec leurs tokens, fait un Split, puis en parallèle envoie un email Gmail et une notification WhatsApp via Twilio. À la fin, il met à jour le statut dans la DB via un PATCH."*

**Basculer sur la boîte Gmail** : montrer l'email reçu

> *"Voici l'email reçu par le professeur, avec un lien sécurisé valide 7 jours."*

**Basculer sur WhatsApp** (si disponible)

> *"Et voici la notification WhatsApp en parallèle."*

### Séquence 5 — Saisie professeur (1 min)

**À l'écran** : cliquer le lien dans l'email → arriver sur le formulaire `/repondre/<token>`

> *"Le professeur clique sur le lien et arrive sur un formulaire personnalisé. Il sélectionne ses créneaux disponibles, par exemple lundi 8h-10h en salle A, mercredi 13h-15h, et soumet."*

**Saisir 2-3 créneaux et soumettre**

> *"En base, son statut passe automatiquement à 'Répondu'."*

**Retour Dashboard** : montrer le statut "✅ Répondu"

### Séquence 6 — Génération de l'EDT avec Claude IA (1 min 30)

**À l'écran** : menu IA → cliquer "Analyser"

> *"Avant de générer, je peux demander à Claude d'analyser les disponibilités. Il me dit s'il y a assez de créneaux, suggère des relances éventuelles."*

**Attendre la réponse Claude**

> *"Claude répond ici en 3-5 secondes."*

**Menu Emploi du temps → Générer**

> *"Maintenant je génère l'EDT pour la filière Développement Web, niveau L3, semaine du 28 avril."*

**Cliquer "Générer"**

> *"L'EDT est créé instantanément côté backend."*

**Cliquer "Voir grille"**

> *"Voici la grille générée. Je peux éditer chaque créneau manuellement si besoin, ou exporter en PDF."*

### Séquence 7 — Envoi aux étudiants (1 min)

**À l'écran** : retour à la liste EDT → cliquer "Envoyer aux étudiants"

> *"Quand je clique 'Envoyer', le backend transmet l'EDT au workflow n8n numéro 2."*

**Basculer sur n8n** : montrer le workflow 2 en exécution

> *"Ce workflow appelle d'abord l'endpoint `/api/ia/optimiser-edt` du backend — c'est ici que Claude réorganise les créneaux pour équilibrer la semaine. Puis n8n génère un email HTML stylé et l'envoie à tous les étudiants de la filière."*

**Basculer sur Gmail étudiant** : montrer l'email reçu

> *"Voici l'email reçu par l'étudiant. La grille est imprimable, et un bandeau indique que l'EDT a été optimisé par Claude IA."*

### Séquence 8 — Système de tickets (45 sec)

**À l'écran** : menu Tickets → cliquer "Nouveau ticket"

> *"EduSchedule intègre aussi un système de tickets pour la maintenance. N'importe quel utilisateur peut signaler un bug, une demande d'évolution ou poser une question."*

**Créer un ticket de démo, le passer en EN_COURS puis RESOLU**

> *"Le support traite les tickets selon des SLA documentés. Statut OUVERT, EN_COURS, RESOLU, avec date de résolution automatique."*

### Séquence 9 — Conclusion (30 sec)

**À l'écran** : revenir au Dashboard avec les stats à jour

> *"En résumé, EduSchedule couvre les 4 blocs de compétences RNCP : cadrage projet documenté, conception et production avec une stack moderne, mise en ligne effective sur Render et Vercel, et maintenance via le système de tickets. Le tout est documenté dans le repo GitHub. Merci pour votre attention !"*

---

## 🎞 Découpage technique recommandé

| Séquence | Durée | Type de plan |
|---|---|---|
| Intro | 0:00 → 0:30 | Webcam + écran |
| Architecture | 0:30 → 1:15 | Écran (doc + schéma) |
| Config données | 1:15 → 2:00 | Écran (CRUD) |
| Bot email/WhatsApp | 2:00 → 3:30 | Écran multi-onglets |
| Saisie prof | 3:30 → 4:30 | Écran (formulaire) |
| Génération + IA | 4:30 → 6:00 | Écran (EDT + IA) |
| Envoi étudiants | 6:00 → 7:00 | Écran multi-onglets |
| Tickets | 7:00 → 7:45 | Écran |
| Conclusion | 7:45 → 8:15 | Webcam |

**Durée totale : ~8 minutes** (un peu plus long que l'objectif initial mais plus complet — vous pouvez accélérer les CRUD).

---

## 🎙 Conseils de tournage

1. **Faire un brouillon écrit** des phrases-clés avant d'enregistrer (pas du mot-à-mot, mais les idées)
2. **Enregistrer en plusieurs prises** par section, monter ensuite
3. **Outils gratuits recommandés** :
   - Capture : OBS Studio, Loom, QuickTime (Mac)
   - Montage : DaVinci Resolve (gratuit, pro), iMovie, Clipchamp
   - Sous-titres : Whisper (auto-transcription) ou directement YouTube
4. **Ajouter des sous-titres FR** pour l'accessibilité
5. **Hébergement vidéo** :
   - YouTube unlisted (lien partageable)
   - Loom (idéal pour démos courtes)
   - Vimeo

---

## 📤 Livrable final

À déposer avec le projet :
- [ ] Lien de la vidéo (YouTube unlisted ou Loom)
- [ ] Le fichier vidéo source en MP4 (au cas où)
- [ ] Mettre le lien dans le `README.md` racine sous "Démo"

---

## ✅ Checklist post-tournage

- [ ] Audio clair, pas de bruits parasites
- [ ] Curseur visible (zoom à 125% si besoin)
- [ ] Pas d'informations sensibles à l'écran (mots de passe, vrais emails persos…)
- [ ] Toutes les fonctionnalités clés montrées (bot, IA, EDT, tickets)
- [ ] Durée entre 5 et 10 minutes
- [ ] Sous-titres FR ajoutés (recommandé)
- [ ] Lien testé en navigation privée
