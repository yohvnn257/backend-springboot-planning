# Guide utilisateur — EduSchedule

> Bloc RNCP 5 : Onboarding & expérience utilisateur

Bienvenue ! Ce guide vous accompagne pas-à-pas dans l'utilisation d'EduSchedule.

---

## 🎯 À qui s'adresse ce guide ?

| Profil | Sections à lire |
|---|---|
| **Administrateur planning** | Sections 1 à 6 (toutes) |
| **Professeur** | Section 4 uniquement |
| **Étudiant** | Section 5 uniquement |
| **Toute personne signalant un bug** | Section 7 |

---

## 1. Première connexion

### 1.1 Accéder à l'application

Rendez-vous sur l'URL de production :
**`https://<votre-application>.vercel.app`**

Vous arrivez sur le **Dashboard**.

> 💡 **Bon à savoir** : la première connexion peut prendre ~30 secondes (le backend redémarre s'il était inactif).

### 1.2 Vue d'ensemble du Dashboard

Le tableau de bord affiche :
- **4 cartes statistiques** : nombre de professeurs, modules, étudiants, EDT générés
- **Bot Email Professeurs** : suivi en temps réel des réponses (rafraîchissement auto toutes les 15s)
- **Boutons d'action** : Actualiser / Envoyer Emails

### 1.3 Navigation

Le menu latéral propose 8 sections :

| Icône | Section | Usage |
|---|---|---|
| 📅 | Dashboard | Vue d'ensemble |
| 👨‍🏫 | Professeurs | Gestion des enseignants |
| 📚 | Modules | Matières/cours enseignés |
| 👩‍🎓 | Étudiants | Liste des étudiants |
| 🗓️ | Disponibilités | Créneaux saisis par les profs |
| 📋 | Emploi du temps | Génération + envoi d'EDT |
| 🤖 | IA | Tableau de bord Claude |
| 🎫 | Tickets | Support utilisateur |

---

## 2. Configurer les données de base

Avant de générer un emploi du temps, il faut renseigner :

### 2.1 Ajouter un professeur

1. Menu → **Professeurs**
2. Bouton **"+ Nouveau professeur"**
3. Remplir :
   - **Nom complet** (ex : "Marie Dupont")
   - **Matière principale** (ex : "Mathématiques")
   - **Email** (utilisé pour le bot)
   - **Téléphone** (format international `+225...` pour WhatsApp)
4. **Enregistrer**

### 2.2 Ajouter un module

1. Menu → **Modules**
2. Bouton **"+ Nouveau module"**
3. Remplir :
   - **Nom du module** (ex : "Algèbre linéaire")
   - **Professeur référent** (sélectionner dans la liste)
   - **Filière** (ex : "Développement Web")
   - **Niveau** (L1, L2, L3, M1, M2)
   - **Salle par défaut** (optionnel)
4. **Enregistrer**

### 2.3 Ajouter un étudiant

1. Menu → **Étudiants**
2. Bouton **"+ Nouvel étudiant"**
3. Remplir :
   - **Nom**
   - **Email** (recevra les EDT)
   - **Filière + Niveau** (doit correspondre à un EDT pour recevoir les emails)
4. **Enregistrer**

> 💡 Les étudiants reçoivent automatiquement les EDT correspondant à **leur filière + niveau exact**.

---

## 3. Lancer le bot de collecte des disponibilités

### 3.1 Cycle automatique (recommandé)

Chaque **lundi à 7h00**, le bot s'exécute automatiquement (cron n8n) :
- Email envoyé à chaque prof
- WhatsApp envoyé en parallèle (si Twilio configuré)
- Lien personnel valide 7 jours

Aucune action manuelle requise.

### 3.2 Lancement manuel

Vous pouvez aussi déclencher le bot à la demande :

1. Dashboard → bouton **"Envoyer Emails"** (vert, en haut à droite)
2. Confirmer
3. Patienter ~5 secondes
4. Vérifier que les statuts passent à **"📧 Email envoyé"** dans la liste

### 3.3 Suivi des réponses

Le Dashboard affiche en temps réel chaque professeur avec son statut :

| Statut | Signification |
|---|---|
| 💤 **Attente** | Aucune action lancée |
| 📧 **Email envoyé** | Bot a envoyé l'email, prof n'a pas encore répondu |
| ✅ **Répondu** | Le prof a soumis ses disponibilités |

Quand **tous les profs ont répondu**, un bandeau vert apparaît avec un bouton "Générer les EDT".

---

## 4. 👨‍🏫 Pour les professeurs : saisir vos disponibilités

### 4.1 Recevoir le lien

Vous recevez chaque lundi matin :
- Un **email** avec un bouton "✏️ Saisir mes disponibilités"
- (Optionnel) un **WhatsApp** avec le même lien

### 4.2 Remplir le formulaire

1. Cliquer sur le lien (valide 7 jours, ne pas le partager)
2. Vous arrivez sur une page personnalisée avec votre nom + vos modules
3. Pour chaque créneau disponible :
   - Choisir le **jour** (Lundi à Vendredi)
   - L'**heure de début** et **fin** (créneaux 2h : 8h-10h, 10h-12h, 13h-15h, 15h-17h)
   - Optionnellement la **salle** souhaitée
4. Ajouter autant de créneaux que vous souhaitez avec **"+ Ajouter un créneau"**
5. Cliquer **"Soumettre mes disponibilités"**

### 4.3 Confirmation

Un message vert confirme la prise en compte. Vous pouvez fermer la page.

---

## 5. 👩‍🎓 Pour les étudiants : recevoir l'EDT

Aucune action requise. Vous recevrez un email automatiquement quand un nouvel emploi du temps est généré pour votre filière et niveau.

L'email contient :
- Une **grille HTML** stylée (semaine du LL/MM au LL/MM)
- Pour chaque créneau : matière, salle, professeur
- Mention "Optimisé par Claude IA" si l'IA a été appliquée

Vous pouvez **imprimer** l'email directement (Ctrl+P) ou le sauvegarder en PDF.

---

## 6. Générer et envoyer un emploi du temps

### 6.1 (Optionnel) Pré-analyse par IA

Avant de générer, vous pouvez demander à Claude d'analyser les disponibilités :

1. Menu → **🤖 IA**
2. Section **"Analyser les disponibilités"** → cliquer **"Analyser"**
3. Claude affiche :
   - Si les disponibilités sont **suffisantes** pour un EDT cohérent
   - Le nombre de créneaux trouvés
   - Des **recommandations** si manque (ex : "relancer le prof X")

### 6.2 Générer l'EDT

1. Menu → **📋 Emploi du temps**
2. Bouton **"+ Générer un EDT"**
3. Renseigner :
   - **Filière** (ex : "Développement Web")
   - **Niveau** (L1 à M2)
   - **Semaine du** / **au** (lundi → vendredi)
4. **Générer**

L'EDT apparaît dans la liste avec le statut **"📋 Généré"**.

### 6.3 Visualiser et éditer

1. Dans la liste → bouton **"👁️ Voir grille"**
2. La grille affiche les 5 jours × 4 créneaux
3. Pour chaque cellule remplie : cliquer **"✏️"** pour éditer le module / professeur / salle
4. Pour exporter en PDF : bouton **"Exporter PDF"** → fenêtre d'impression du navigateur

### 6.4 Envoyer aux étudiants

1. Retour à la liste des EDT
2. Bouton **"📤 Envoyer aux étudiants"** sur la ligne de l'EDT
3. Le système :
   - Envoie l'EDT au workflow n8n
   - n8n appelle Claude pour optimiser une dernière fois
   - n8n envoie un email HTML à chaque étudiant de la filière+niveau
4. Le statut passe à **"✅ Envoyé"**

> ⏱ **Temps moyen** : 10-30 secondes selon le nombre d'étudiants.

---

## 7. 🎫 Signaler un bug ou demander une évolution

### 7.1 Créer un ticket

1. Menu → **🎫 Tickets**
2. Bouton **"+ Nouveau ticket"**
3. Remplir :
   - **Titre** (ex : "Le bouton Envoyer ne réagit pas")
   - **Description** détaillée (étapes pour reproduire, capture si possible)
   - **Type** :
     - `BUG` : quelque chose ne fonctionne pas
     - `EVOLUTION` : nouvelle fonctionnalité souhaitée
     - `QUESTION` : besoin d'aide
   - **Email** : votre adresse pour la réponse
4. **Envoyer**

### 7.2 Suivre l'avancement

Tous les tickets sont visibles dans la liste avec leur statut :

| Statut | Signification |
|---|---|
| 🔴 **OUVERT** | En attente de prise en charge |
| 🟡 **EN_COURS** | Le support travaille dessus |
| 🟢 **RESOLU** | Problème résolu |

---

## 8. Foire aux questions

### "Le bouton 'Envoyer Emails' ne fait rien"

→ Vérifier que :
1. n8n est démarré et le workflow 1 est **activé**
2. Les variables `N8N_EMAIL_TRIGGER_URL` sont à jour dans Render
3. Au moins **un professeur avec email** existe en base

### "Mon prof a cliqué sur le lien mais le formulaire est vide"

→ Vérifier que le **module** du prof est bien créé. Sans module associé, le prof n'a rien à remplir.

### "Claude IA dit 'Clé API manquante'"

→ Demander à l'admin de vérifier `ANTHROPIC_API_KEY` dans Render. Sans clé, l'EDT est quand même généré (mais sans optimisation).

### "Je ne reçois pas l'EDT par email"

→ Vérifier que :
1. Vous êtes inscrit comme **étudiant** dans la même filière + niveau que l'EDT
2. Votre email est correct
3. L'email n'est pas dans les spams

### "Le numéro WhatsApp Twilio ne marche pas"

→ Le numéro doit être au format international **`+225...`** (Côte d'Ivoire) ou **`+33...`** (France). Sans le `+pays`, Twilio refuse l'envoi.

---

## 9. Raccourcis utiles

| Action | Raccourci |
|---|---|
| Aller au dashboard | Cliquer sur le logo en haut |
| Imprimer un EDT | Cliquer "Exporter PDF" puis Ctrl+P |
| Rafraîchir la liste | Bouton "Actualiser" |
| Filtrer les tickets | Cliquer sur les badges (Ouverts / En cours / Résolus) |

---

## 10. Vous êtes bloqué ?

1. Consultez d'abord cette FAQ
2. Créez un ticket de type **QUESTION**
3. Contactez le support : **yohanntoure40@gmail.com**

---

📺 **Vidéo de démonstration** : voir [SCRIPT_DEMO_VIDEO.md](SCRIPT_DEMO_VIDEO.md) pour le scénario complet en images.
