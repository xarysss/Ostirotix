# Suivi du projet Ostirotix

Jeu Android de devinette sémantique.
**Nom : Ostirotix** (renommé sur demande : Flamot → Cemantix → Ostirotix, le 2026-06-11 ;
remplacé partout : dossier, package `com.ostirotix.app`, UI, backend, docs, base `ostirotix.db`).

## État : REDESIGN COMPLET + PRÊT PUBLICATION ✅ — AAB release signé ✅ — testé sur émulateur ✅

## Session 3 (2026-06-12) : refonte DA « vieux dictionnaire » + publiable
- **Refonte UI complète** : palette bois sombre/parchemin/or/cuir (WoodDark #17100A, Parchment #EFE3C4,
  GoldOld #C9A227, SealRed #8C2B1A), police EB Garamond (res/font, OFL), zéro emoji (icônes vectorielles :
  coin, page, ink, book, quill, swords, trophy, bookmark, gear).
- **Économie** : pièces/pages rares/encre (Prefs), 6 améliorations (Economy.kt : biblio, loupe, encrier,
  scribe, archives, registre ; coût ×1.5/niveau), récompenses victoire (essais+vitesse+maîtrise+série),
  indices payés en encre, recharge encre 50 pièces → 25.
- **Écrans réécrits/créés** : Home (BookCover+sceau+marque-page série+nav basse), SoloGame (SealGauge,
  registre parchemin, plume), Result (sceau brisé, récompenses), MiscScreens (Options/Classement/Profil),
  MultiScreens (Duel/Antichambre/MultiGame/Résultats ELO), **LibraryScreen** (boutique améliorations).
- **Bugs corrigés** : 4 emojis restants dans MultiViewModel ; themes.xml et icône launcher encore aux
  couleurs Flamot (flamme orange → livre parchemin/sceau sur bois) ; **anti-farm mot du jour** (rejouer
  le daily gagné ne rapporte plus rien) ; relance du daily une fois terminé (NavGraph).
- **Publication** : network_security_config (HTTP limité à 10.0.2.2/localhost, HTTPS ailleurs),
  R8+shrinkResources activés, règles ProGuard (OkHttp/modèles), keystore release généré
  (`android-app/keystore/ostirotix-release.jks`, config dans `keystore.properties` — NE PAS PERDRE !),
  signature branchée dans Gradle. **AAB signé : 4,05 Mo** (`app/build/outputs/bundle/release/app-release.aab`).
- **Testé sur émulateur** : daily (livre→23°, jauge, meilleure piste), Home, Bibliothèque (achat Scribe
  100 pièces → Niv.1, +2 pièces/partie, boutons grisés si fauché), Options, Profil, Duel (compte invité).
  Captures : `screen_*.png` à la racine Ostirotix/.
- **Options refaites version joueur** : Compte (connexion/invité/déconnexion via MultiViewModel),
  Langue (Français actif, English à venir), Jeu (haptique+tutoriel), Boutique (→ Bibliothèque),
  Confidentialité et mentions (politique de confidentialité, CGU, licences open source,
  **Supprimer mes données** RGPD = Prefs.clearAll()+logout, dialogues parchemin). URL serveur
  visible **uniquement en debug** (BuildConfig.DEBUG, buildConfig=true dans Gradle). Footer version.
  Testé sur émulateur (compte « Xarys » affiché, dialogue confidentialité OK). AAB régénéré (4,07 Mo).

## Session 2 (2026-06-11) : émulateur sans Android Studio — RÉUSSI
- Installé : JDK 17 Temurin portable → `C:\Users\xaris\jdk\jdk-17.0.19+10` ; SDK Android →
  `%LOCALAPPDATA%\Android\Sdk` (platform-tools, android-35, build-tools 35, emulator, image x86_64) ;
  licences acceptées via fichiers `licenses/` ; AVD « pixel7 » créé.
- **BUILD SUCCESSFUL en 3 min 47, zéro erreur de compilation Kotlin du premier coup.**
- App installée et testée sur émulateur : tutoriel 3 bulles OK, "gare" → 99°C n°1 999‰ + badge
  record + jauge rouge + halo, "train" → écran victoire (mot révélé, 2 essais, série 1 jour, partage).
- Captures dans `docs/` (accueil, 99°C, victoire). APK : `android-app/app/build/outputs/apk/debug/app-debug.apk`.
- Commandes utiles : build = `$env:JAVA_HOME="C:\Users\xaris\jdk\jdk-17.0.19+10"; .\gradlew.bat assembleDebug` ;
  émulateur = `%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe -avd pixel7` ;
  install = `adb install -r app-debug.apk` ; lancer = `adb shell am start -n com.ostirotix.app/.MainActivity`.

## Session 3 (suite) : bots ranked « élastiques »
- **backend/bots.py** : bot adaptatif à 2 modes. OMBRE : reste quelques degrés derrière le meilleur
  humain (mots froids du dico si même le voisin le plus lointain serait trop chaud). CHASSE : si le
  joueur stagne <50° après PATIENCE (120 s) ou si la partie dépasse HARD_LIMIT (420 s), le rang du bot
  décroît vite jusqu'au mot exact — jamais avant MIN_WIN (210 s). Réglables par env BOT_PATIENCE etc.
  Élastique dans les 2 sens : si le joueur remonte ≥50°, le bot repasse en ombre.
- Noms persistants avec ELO réel en BDD : Le Scribe, L'Archiviste, Maître Plume, Dame Encre,
  Le Copiste, Frère Vélin (db.get_user_by_username ajouté).
- rooms.py : Player.is_bot, _send() (ws=None silencieux), start() lance bot_loop, finish() annule
  les tâches, add_bot() ; main.py : RoomReq.bot, nettoyage lobby ignore les bots ; le bot clôt la
  partie si tous les humains sont déconnectés.
- App : ApiClient/MultiViewModel param bot, bouton « Duel classé contre un bot » dans Duel lexical.
- **Testé E2E** (backend/test_bot.py, websocket réel, délais raccourcis) : lobby OK, ombre derrière le
  joueur, chasse quand le joueur stagne, recul quand il remonte, victoire bot 100° après MIN_WIN,
  ELO ±19/20 appliqué. Testé aussi depuis l'app sur émulateur (Dame Encre joue en direct).
- Fix visuel : PlayerHeatBar passé aux couleurs parchemin (noms illisibles avant).

## Étapes
- [x] 1. Création dossiers, nom initial Flamot → renommé Ostirotix sur demande
- [x] 2. Script Python générateur + semantic_pack_demo.json (50 secrets, ~420 voisins, dico 1317 mots)
- [x] 3. Backend FastAPI complet : db.py, semantic.py, elo.py, rooms.py, main.py, seed_demo.py
- [x] 4. Projet Android : Gradle (AGP 8.6.1, Kotlin 2.0.20, Compose BOM 2024.09), manifest, thème, icône adaptative
- [x] 5. Android data : Models, SemanticEngine (formules = backend), Prefs, ApiClient (REST+WS), ServiceLocator
- [x] 6. Android UI : 3 ViewModels, 10 écrans, composants (HeatGauge, GuessRow, PlayerHeatBar, tutoriel 3 bulles), navigation
- [x] 7. Gradle wrapper téléchargé (gradlew, gradlew.bat, jar v8.9)
- [x] 8. Pack JSON copié dans assets Android + backend
- [x] 9. README.md racine complet
- [x] 10. Backend testé en réel : E2E ranked 2 joueurs WebSocket OK (Alice exact → +20 ELO, Bob 97°C → −20)

## Tests effectués
- Moteur sémantique Python : exact=100°C, rang 1=99°C, mot dico hors liste=froid déterministe, inconnu=glacial+non reconnu ✅
- ELO pairwise K=40 : ±20 sur 2 joueurs à 1000 ✅
- E2E complet `python seed_demo.py` (backend lancé) : room, WS, points, bonus vitesse, timer 30 s, persistance, leaderboard, profil ✅
- **Non testé : compilation Android** (ni Java ni Android Studio sur cette machine). Code écrit avec soin mais
  de possibles erreurs mineures de compilation peuvent rester → les corriger à la 1re synchro Gradle.

## Décisions clés
- Température par RANG : exact=100 ; top10=90-99 ; top100=70-89 ; top500=45-69 ; top1000=30-44 ;
  mot du dico hors liste = froid déterministe −20..29 (md5 8 hex % 50) ; inconnu = −90..−50 + "non reconnu".
- Progression ‰ = 1000 − rang (exact = 1000‰).
- Mot du jour : (toordinal×31+7) % 50 — formule identique Kotlin (epochDay+719163) et Python ✅ même mot partout.
- Multi : points 50-69→+1, 70-84→+3, 85-94→+7, 95-99→+15, exact→+100 ; bonus vitesse 1er à 70/85/95 = +5/+10/+20 ;
  cooldown 1,5 s ; perf = bonusExact(1000) + points + tempMax×2 + vitesse − spam ; timer fin 30 s.
- ELO pairwise, départ 1000, K=40/24/16, égalité 0.5 si |Δperf|<10 à statut "trouvé" égal.
- Solo 100% hors-ligne (asset). Multi via http://10.0.2.2:8000 (modifiable dans Paramètres).
- Pas de DI lib ; org.json ; OkHttp (REST+WS) ; navigation-compose ; haptics ≥70/90°C ; DA « vieux dictionnaire » (bois/parchemin/or/cuir, EB Garamond, zéro emoji).
- startDestination = solo mot du jour (jouable en <10 s, sans compte) ; tutoriel 3 bulles non bloquant.

## Reste à faire (prochaine session)
1. Tester multi app↔backend en réel (2 émulateurs ou émulateur+seed_demo.py comme 2e joueur).
2. Play Store : compte développeur (25 $), fiche store (titre, descriptions, captures 1080×1920,
   icône 512px, bannière 1024×500), questionnaire contenu, politique de confidentialité (URL publique),
   puis upload de `app-release.aab` en test interne → production.
3. Pour le multi en production : héberger le backend en HTTPS et changer l'URL dans Options.
4. Polish éventuel : sons, particules de poussière dorée, plus de mots secrets dans le pack.

## Mémo reprise rapide
- Backend : `cd Ostirotix/backend && uvicorn main:app --host 0.0.0.0 --port 8000` (db: ostirotix.db, reset = supprimer le fichier).
- Test E2E : backend lancé puis `python seed_demo.py` (~40 s).
- Régénérer données : `python Ostirotix/data/generate_semantic_pack.py` (écrit les 3 copies).
- Écrans : Home, SoloGame, Result, MultiMode, Lobby, MultiGame, RankedResult, Leaderboard, Profile, Settings, Library.
- Release : `.\gradlew.bat bundleRelease` → AAB signé (keystore.properties requis à la racine android-app).
- ViewModels : SoloViewModel, MultiViewModel (WS), AccountViewModel — partagés au niveau NavGraph.
