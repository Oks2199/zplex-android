# Movynex

Movynex est une application Android privée destinée à parcourir et lire une vidéothèque familiale stockée sur Google Drive. Elle indexe les films et séries à partir de conventions de nommage, récupère les métadonnées auprès de TMDB et OMDb, puis utilise MPV pour la lecture.

Le projet est un fork familial de [ZPlex](https://github.com/ZPlexLabs/zplex-android). Il n'est pas destiné à être publié sur le Google Play Store.

Pour reprendre le développement dans une nouvelle conversation, lire intégralement et dans cet ordre [`AGENTS.md`](AGENTS.md), [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md), [`progress.md`](progress.md), puis ce `README.md`, avant toute action sur le projet.

## Version actuelle

- Version livrée : `1.0.7` (code 12 sur téléphone, code 13 sur Fire TV)
- Fonctionnement confirmé par l'utilisateur le 12 novembre 2026 sur son téléphone Android et son Fire TV Stick sous Fire OS 8.
- APK téléphone livrée : `H:\Downloads\Movynex-1.0.7.apk`
- APK Fire TV livrée : `H:\Downloads\Movynex-1.0.7-FireTV-ARMv7.apk`
- Téléphone : Android 12 / API 31 minimum, ARM64, package `com.cursedcrew.movynex`, signature Cursed Crew.
- Fire TV : Fire OS 8 (Android 11 / API 30), ARM 32 bits, package `com.cursedcrew.movynex.firetv.debug` conservé pour les mises à jour.
- Google Drive : lecture des médias et synchronisation du profil dans l'espace privé de Movynex
- Statut OAuth : externe, en production

## Fonctionnalités

- Indexation de dossiers Films et Séries choisis sur Google Drive.
- Lecture en streaming avec MPV.
- Téléchargement hors ligne dans l'espace privé de l'application.
- Reprise de lecture et historique enregistrés localement.
- Le badge « Vu » identifie les films terminés dans la Bibliothèque, Ma liste et leurs rangées d'accueil. Les épisodes terminés portent le badge dans leur saison.
- Accueil organisé autour du contenu personnel : grande bannière prioritairement issue du Drive, lectures réellement commencées, ajouts Drive récents et Ma liste lorsqu'elle n'est pas vide. La bannière utilise une affiche verticale `2:3` sur téléphone et un visuel en paysage sur TV. Les suggestions séparées affichent ensuite les tendances TMDB de la semaine, les films actuellement au cinéma et les films ou séries disponibles en streaming en France.
- Affiches, titres, résumés, dates et autres métadonnées via TMDB et OMDb, demandés en français auprès de TMDB avec la région France. Les affiches sont présentées dans des cadres `2:3` uniformes sans déformation.
- Disponibilités françaises des films via TMDB/JustWatch : sortie cinéma récente, streaming par abonnement, location et achat.
- Le bouton principal d'une fiche ne propose la lecture que lorsqu'un fichier Drive ou hors ligne est réellement disponible ; sinon il indique le type de disponibilité externe.
- La Bibliothèque contient uniquement les films et séries détectés sur Google Drive. Ma liste contient les médias ajoutés manuellement qui ne sont pas encore présents sur le Drive.
- Lorsqu'un média de Ma liste est détecté pendant l'indexation, il passe automatiquement dans la Bibliothèque sans doublon.
- Sur une fiche, le bouton indique clairement « Ajouter à ma liste », « Dans ma liste » ou « Dans la bibliothèque ».
- Le bouton « Saisons et épisodes » des fiches de séries conserve l'accès à toutes les saisons et à tous les épisodes, même lorsqu'aucun fichier n'est présent dans Movynex.
- Les plateformes françaises sont affichées au niveau de la série puis de la saison consultée. Ces données TMDB/JustWatch décrivent les offres déclarées pour l'œuvre ou la saison en France et ne garantissent pas la disponibilité de chaque épisode.
- Dans une saison, chaque épisode indique clairement s'il est téléchargé, présent sur le Drive ou absent du Drive. Un épisode absent reste consultable à titre informatif, mais ne lance pas le lecteur.
- Interface française, y compris les réglages, le lecteur, les notifications, les téléchargements et les messages d'erreur à partir de la version 1.0.3.
- Navigation principale à trois destinations : Accueil, Bibliothèque et Ma liste. Sur TV, la barre supérieure réunit les trois destinations, la loupe et les réglages. Sur téléphone, les destinations se trouvent en bas et la recherche dans les barres supérieures.
- Les tendances hebdomadaires sont celles de TMDB à l'échelle mondiale, avec des métadonnées françaises. Les rangées cinéma et streaming sont en revanche filtrées sur la France ; les plateformes proviennent de TMDB/JustWatch et conservent leur attribution.
- Mode image dans l'image pris en charge par le lecteur.

### Actualiser les informations mises en cache

Les métadonnées et disponibilités externes sont mises en cache pour limiter les appels réseau. Dans les réglages, un appui long sur le compteur **Éléments en cache** ouvre la confirmation permettant de vider ce cache. Si les métadonnées d'un film ou d'une série déjà indexé restent anciennes, retirer ensuite ce média de la bibliothèque par glissement puis relancer l'indexation ; l'historique de lecture est conservé séparément.

## Organisation de Google Drive

Les dossiers peuvent porter n'importe quel nom : l'utilisateur sélectionne séparément son dossier de films et son dossier de séries dans Movynex.

### Films

Les vidéos doivent être placées directement à la racine du dossier de films sélectionné.

Format reconnu :

```text
Titre (Année) [TMDB_ID].mkv
Titre (Année) [TMDB_ID].mp4
```

Exemples :

```text
Avatar (2009) [19995].mkv
Inception (2010) [27205].mp4
```

Les extensions actuellement reconnues par l'indexeur sont `.mkv` et `.mp4` en minuscules. L'identifiant numérique entre crochets est celui de l'œuvre sur [TMDB](https://www.themoviedb.org/).

### Séries

Chaque série possède son propre dossier à la racine du dossier de séries sélectionné :

```text
Nom de la série (Année) [TMDB_ID]/
└── Season 1/
    ├── Nom de la série - S01E01 - Titre de l'épisode.mkv
    └── Nom de la série - S01E02 - Titre de l'épisode.mkv
```

Règles importantes :

- le dossier de la série suit le format `Nom (Année) [TMDB_ID]` ;
- les dossiers de saisons se nomment `Season 1`, `Season 2`, etc. ;
- le nom d'un épisode doit contenir un identifiant comme `S01E01` ;
- les fichiers vidéo d'une saison sont placés directement dans son dossier `Season N`.

## Configuration de Google Drive

Pour compiler une APK privée préconfigurée, renseigner ces deux propriétés dans le fichier local `local.properties`, ignoré par Git.

```properties
MOVYNEX_DRIVE_CLIENT_ID=
MOVYNEX_DRIVE_CLIENT_SECRET=
```

Ces valeurs sont le Client ID et le Client Secret de Movynex dans Google Cloud. La compilation les inclut dans l'APK privée. Réserver cette APK à l'usage personnel. Une compilation avec les deux propriétés vides propose de saisir ces deux valeurs dans l'application.

La configuration Google Cloud utilisée par la famille est la suivante :

- projet : **Movynex Personal** ;
- application OAuth : **Movynex** ;
- client : **Movynex Android** ;
- type de client : application de bureau ;
- scopes : `https://www.googleapis.com/auth/drive.readonly` pour la bibliothèque et `https://www.googleapis.com/auth/drive.appdata` pour les données privées de synchronisation ;
- URI de redirection : `http://127.0.0.1:53682/` ;
- audience : externe, statut **In production**.

Pages publiques associées à l'écran de consentement :

- [Accueil Movynex](https://sites.google.com/view/movynex/home)
- [Politique de confidentialité](https://sites.google.com/view/movynex/privacy-policy)
- [Conditions d'utilisation](https://sites.google.com/view/movynex/terms-of-service)

### Première connexion

1. Ouvrir **Réglages → Connexion Google Drive** sur le téléphone ou le Fire TV.
2. Appuyer sur **Connecter Google Drive**. Si les champs s'affichent, renseigner le Client ID et le Client Secret de Movynex.
3. Choisir le compte et autoriser la lecture de Drive et la synchronisation dans le navigateur de cet appareil. Les identifiants du compte se saisissent sur la page Google si elle les demande.
4. Lorsque le navigateur affiche **Movynex**, revenir dans l'application. Elle récupère le code localement et enregistre la session.
5. Sélectionner les dossiers de films et de séries, puis lancer l'indexation.

Le statut OAuth en production évite l'expiration automatique au bout de sept jours propre au mode de test. Comme l'application familiale n'est pas validée publiquement par Google, un avertissement « application non validée » peut apparaître lors de la première connexion et le projet reste soumis à une limite de 100 utilisateurs OAuth.

### Synchronisation entre téléphone et TV

Movynex partage **Ma liste** (ajouts et retraits) et les progressions de lecture des films et épisodes avec un seul profil associé au compte Google. Cela permet de reprendre un titre et de retrouver son badge « Vu » sur l'autre appareil. Ouvrir **Réglages → Synchronisation du profil** sur le téléphone en premier. Sur une installation déjà connectée, choisir **Autoriser la synchronisation** et reprendre le même compte Google. Répéter sur la TV. Sa première synchronisation reprend Ma liste et les progressions du téléphone, puis les changements sont envoyés dans les deux sens. L'écran indique l'état de la synchronisation.

Movynex conserve les données de synchronisation dans l'espace privé de l'application sur Drive (`appDataFolder`). Les modifications locales en attente sont envoyées au retour de la connexion. Les dossiers Drive sélectionnés, le catalogue indexé et les fichiers téléchargés restent propres à chaque appareil.

## Installation et mises à jour

Installer l'APK de l'appareil par-dessus la version existante. Pour que la mise à jour conserve la connexion Google Drive, les dossiers sélectionnés, l'historique et les téléchargements, elle doit garder le même package et le même certificat de signature :

- Téléphone : `com.cursedcrew.movynex` et certificat Cursed Crew.
- Fire TV : `com.cursedcrew.movynex.firetv.debug` et certificat de l'installation Fire TV existante. Le suffixe `.debug` fait partie du package historique de la release Fire TV.

Ne pas désinstaller l'application avant une mise à jour : une désinstallation supprime ses données locales. Pour envoyer et installer l'APK sur le Stick depuis un PC, voir la [procédure ADB](PROJECT_CONTEXT.md#installation-sur-le-fire-tv-stick-depuis-un-pc).

## Développement

### Prérequis

- JDK 17
- Android SDK 36 et outils en ligne de commande (Android Studio est facultatif)
- Git

### Installation locale

```bash
git clone https://github.com/Oks2199/zplex-android.git
cd zplex-android
```

Créer ou compléter `local.properties` sans le committer :

```properties
sdk.dir=C\:/chemin/vers/Android/Sdk
TMDB_API_KEY=votre_cle_tmdb
OMDB_API_KEY=votre_cle_omdb
```

Tests et compilation debug :

```bash
./gradlew testDebugUnitTest assembleDebug
```

Sous Windows :

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Dans certaines sessions Codex Windows, Gradle peut échouer avec `Unable to establish loopback connection` avant même de configurer le projet. Cette erreur vient de l'environnement d'exécution et ne prouve pas que le code Android ne compile pas. Dans ce cas, la validation de référence doit être effectuée avec GitHub Actions ; il est inutile de modifier `gradle.properties` ou le code applicatif pour tenter de corriger cette erreur.

Le namespace Kotlin historique `zechs.zplex` est volontairement conservé en interne. Il ne correspond pas au package Android public et ne doit pas être renommé sans migration planifiée.

### Signature release

La configuration release attend les variables d'environnement suivantes :

```text
MOVYNEX_KEYSTORE_PATH
MOVYNEX_KEYSTORE_PASSWORD
MOVYNEX_KEY_ALIAS
MOVYNEX_KEY_PASSWORD
```

Leurs valeurs ne doivent jamais être ajoutées au dépôt, à la documentation ou aux journaux.

### Mise à jour Fire TV

Pour un Fire TV Stick sous Fire OS 8 (Android 11) qui exécute les applications en ARM 32 bits, construire la release Fire TV.

```powershell
.\gradlew.bat -PmovynexFireTv=true :app:testFireTvReleaseUnitTest :app:assembleFireTvRelease :app:lintFireTvRelease
```

L'APK se trouve dans `app\build\outputs\apk\fireTv\release\`. Son identifiant et son certificat permettent de mettre à jour l'installation Fire TV existante et de conserver ses données.

Sur téléviseur, la barre en haut regroupe Accueil, Bibliothèque, Ma liste, la recherche et les réglages. Les flèches déplacent la sélection, le bouton central ouvre le titre ou active la commande et Retour remonte à l'écran précédent. Dans le lecteur, Menu affiche ou masque les commandes et les touches de lecture permettent de mettre en pause ou de se déplacer dans la vidéo. Dans la Bibliothèque ou Ma liste, un appui long sur une affiche propose de la retirer.

Dans les fenêtres de choix, utilise les flèches pour parcourir les options et le bouton central pour valider. Les confirmations placent la sélection sur le bouton d'annulation lorsqu'il est disponible.

## Intégration continue

Le workflow [`.github/workflows/android.yml`](.github/workflows/android.yml) :

- utilise Java 17 ;
- exécute les tests unitaires debug ;
- construit les APK debug par architecture ;
- construit une APK release ARM64 signée lors des exécutions hors pull request ;
- récupère TMDB, OMDb et la signature depuis les secrets GitHub.

Un push sur `main` déclenche automatiquement ce workflow. Une pull request vers `main` exécute les tests et construit le debug, mais ignore volontairement la release signée. Un lancement manuel ne teste que le contenu déjà poussé sur la branche distante sélectionnée : il n'inclut jamais les modifications non committées du poste local. Movynex 1.0.6 a été validée par la [pull request n°3](https://github.com/Oks2199/zplex-android/pull/3) et [GitHub Actions n°21](https://github.com/Oks2199/zplex-android/actions/runs/34784323576), puis construite, signée et vérifiée par [GitHub Actions n°22](https://github.com/Oks2199/zplex-android/actions/runs/34784568126). L'APK livrée se trouve dans `H:\Downloads\Movynex-1.0.6.apk`.

## Confidentialité et sécurité

- Movynex accède aux médias Google Drive en lecture seule. La synchronisation du profil écrit dans l'espace Drive privé de l'application.
- Les identifiants OAuth, jetons, préférences, index et progressions sont conservés dans l'espace privé de l'application sur l'appareil.
- Les fichiers hors ligne sont enregistrés dans le dossier privé `movynex-downloads` de l'application et disparaissent lors de sa désinstallation.
- Les titres et identifiants nécessaires peuvent être envoyés à TMDB et OMDb pour récupérer les métadonnées.
- Ne jamais partager une capture, une URL ou un journal contenant un Client ID, un Client Secret, un code d'autorisation ou un jeton OAuth.

## Crédits

- [ZPlex](https://github.com/ZPlexLabs/zplex-android) — projet d'origine
- [DriveStream](https://github.com/itszechs/DriveStream) — idées liées à Google Drive et MPV
- [TheMovieDB](https://www.themoviedb.org/) — métadonnées et recherche
- [JustWatch](https://www.justwatch.com/) — données de disponibilité sur les plateformes fournies via TMDB
- [OMDb API](https://www.omdbapi.com/) — métadonnées complémentaires
- [FileBot](https://www.filebot.net/) — conventions de nommage
- [Plex](https://www.plex.tv/) — inspiration du concept
- [mpv-android](https://github.com/mpv-android/mpv-android) — base des scripts MPV

## Licence

Le projet reste distribué sous la [licence MIT](LICENSE). La notice de copyright du projet d'origine doit être conservée.
