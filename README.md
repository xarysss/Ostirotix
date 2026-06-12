# Ostirotix

Jeu Android de devinette **sémantique** : trouve le mot secret en proposant des mots.
Plus ton mot est proche **par le sens**, plus la température monte (de −100°C à 100°C).
Version mobile maison du principe Cémantix, avec son propre design et son propre moteur.

- **Solo hors-ligne immédiat** : mot du jour + entraînement illimité, sans compte.
- **Multijoueur temps réel** (fun ou ranked ELO) via un petit backend local FastAPI.

## Structure

```
Ostirotix/
├── android-app/     Appli Android (Kotlin + Jetpack Compose, MVVM)
├── backend/         FastAPI + WebSocket + SQLite (rooms, points, ELO)
├── data/            Pack sémantique de démo + script générateur
├── suivi.md         Journal de développement
└── README.md
```

## 1. Lancer l'app Android (émulateur)

1. Ouvrir **Android Studio** → *Open* → sélectionner le dossier **`Ostirotix/android-app`**.
2. Laisser la synchro Gradle se faire (télécharge les dépendances, 1re fois ~2-5 min).
3. Créer/choisir un émulateur **Pixel 7 ou 8** (API 34+ recommandé) : *Device Manager* → *Create device*.
4. Cliquer **Run ▶**. L'app s'ouvre **directement sur une partie solo** (mot du jour).

> Le solo fonctionne **sans backend et sans compte** : le moteur sémantique est embarqué
> (asset `semantic_pack_demo.json`, 50 mots secrets, ~420 voisins chacun).

### Comment jouer
Écris un mot → reçois une température + un rang (`n°X`) + une progression (‰) si tu es
dans le top 1000. Froid → tiède → chaud → brûlant → mot exact = victoire.
3 indices disponibles (coûtent de l'encre), bouton « Révéler » pour abandonner.
Les victoires rapportent des pièces ; la Bibliothèque permet d'acheter 6 améliorations.

## 2. Lancer le backend (multijoueur)

```bash
cd Ostirotix/backend
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

- Healthcheck : http://localhost:8000/health
- L'émulateur joint le PC hôte via **http://10.0.2.2:8000** (déjà préconfiguré dans l'app,
  modifiable dans Paramètres).

### Tester le multi avec 2 clients
Lance **2 émulateurs** (ou 1 émulateur + modification de l'URL serveur sur un téléphone réel
en réseau local). Dans l'app : *Multijoueur* → créer un compte ou jouer invité →
*Créer une room* (note le code) → l'autre client *Rejoindre* avec le code → l'hôte lance.
Premier au mot exact gagne ; les autres ont 30 s pour sécuriser des points.

## 3. Règles multijoueur / ELO

- Points de manche : 50-69°C **+1** · 70-84 **+3** · 85-94 **+7** · 95-99 **+15** · exact **+100** + victoire.
- Bonus vitesse au **premier** à 70°C (+5), 85°C (+10), 95°C (+20).
- Anti-spam : cooldown 1,5 s, pénalité sur essais invalides et sur volume d'essais anormal.
- Score de perf = bonusExact + pointsProximité + tempMax×2 + bonusVitesse − pénalitéSpam.
- **ELO pairwise** (départ 1000) : comparaison de chaque paire de joueurs,
  attendu = 1/(1+10^((Rb−Ra)/400)), K = 40 (<10 parties), 24, 16 (>1800).

## 4. API backend

| Méthode | Route | Rôle |
|---|---|---|
| GET | `/health` | healthcheck |
| POST | `/auth/register` | création compte simple |
| POST | `/auth/guest` | login invité |
| GET | `/daily` | index du mot du jour |
| POST | `/solo/guess` | soumission mot solo (variante en ligne) |
| POST | `/rooms` | créer une room (fun ou ranked) |
| POST | `/rooms/{code}/join` | rejoindre une room |
| WS | `/ws/room/{code}?userId=` | partie temps réel |
| GET | `/leaderboard` | classement ELO |
| GET | `/profile/{id}` | profil joueur |

## 5. Régénérer / enrichir les données

```bash
cd Ostirotix/data
python generate_semantic_pack.py   # réécrit le pack dans data/, backend/ et les assets Android
```

Le moteur (Kotlin `SemanticEngine` + Python `semantic.py`) partage les **mêmes formules** ;
pour passer à un vrai modèle (word2vec/FastText/SentenceTransformers), il suffit de produire
le même format JSON `{secrets:[{word, neighbors:[[mot, score],…]}], dictionary:[…]}`
ou de brancher l'app sur `POST /solo/guess`.

## 6. Publication Play Store

Le projet est **prêt à publier** : R8/minify activés, ressources réduites, trafic réseau
sécurisé (HTTP limité au dev local), signature release configurée.

```powershell
cd Ostirotix\android-app
$env:JAVA_HOME = "C:\Users\xaris\jdk\jdk-17.0.19+10"
.\gradlew.bat bundleRelease
# → app\build\outputs\bundle\release\app-release.aab (signé, ~4 Mo)
```

> La signature lit `keystore.properties` + `keystore/ostirotix-release.jks`.
> **Sauvegarde ces deux fichiers précieusement** : sans eux, impossible de mettre à jour
> l'app sur le Play Store. Ne les partage jamais.

Étapes concrètes sur [Play Console](https://play.google.com/console) :
1. Créer un compte développeur (25 $ une seule fois).
2. *Créer une application* → nom « Ostirotix », langue française, type Jeu, gratuit.
3. Remplir la fiche : description courte/longue, icône 512×512, bannière 1024×500,
   au moins 2 captures d'écran par format (les `screen_*.png` peuvent servir de base).
4. Questionnaires : classification du contenu (PEGI 3), sécurité des données
   (aucune donnée collectée en solo), public cible (13+ conseillé).
5. Politique de confidentialité : URL publique requise (page statique suffisante).
6. *Tests internes* → téléverser `app-release.aab` → ajouter ton email testeur → valider.
7. Après vérification, promouvoir en **production**.

## 7. Améliorations futures

- Vrais embeddings (FastText fr) côté backend + cache des rangs.
- Matchmaking automatique par MMR caché, reconnexion en cours de partie.
- Comptes avec mot de passe + tokens, sauvegarde cloud des stats solo.
- Animations de particules sur mot brûlant, sons.
- Mode duel 1v1 rapide, tournois, ligues hebdomadaires.
- Backend hébergé en HTTPS pour le multi en production.

## Dépannage

- **Gradle sync échoue** : vérifier la connexion internet (téléchargement des dépendances) ;
  Android Studio Koala+ recommandé (AGP 8.5).
- **Multi : « backend injoignable »** : le backend doit tourner sur le PC hôte, port 8000 ;
  depuis l'émulateur c'est `10.0.2.2`, pas `localhost`.
- **Réinitialiser la base** : supprimer `backend/ostirotix.db` (recréée au démarrage).
