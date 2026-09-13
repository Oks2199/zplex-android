# Movynex

Movynex est une application Android privée destinée à parcourir et lire une vidéothèque familiale stockée sur Google Drive. Elle indexe les films et séries à partir de conventions de nommage, récupère les métadonnées auprès de TMDB et OMDb, puis utilise MPV pour la lecture.

Le projet est un fork familial de [ZPlex](https://github.com/ZPlexLabs/zplex-android). Il n'est pas destiné à être publié sur le Google Play Store.

Pour reprendre le développement dans une nouvelle conversation, commencer par [`AGENTS.md`](AGENTS.md), puis lire le [`dossier de reprise`](PROJECT_CONTEXT.md) et [`progress.md`](progress.md).

## Version actuelle

- Version du code courant : `1.0.3` (`versionCode = 7`)
- Dernière APK livrée et vérifiée : `1.0.3` (`versionCode = 7`)
- Package Android : `com.cursedcrew.movynex`
- Android minimum : Android 12 / API 31
- APK de référence : ARM64, signée avec la clé Cursed Crew
- Accès Google Drive : lecture seule
- Statut OAuth : externe, en production

## Fonctionnalités

- Indexation de dossiers Films et Séries choisis sur Google Drive.
- Lecture en streaming avec MPV.
- Téléchargement hors ligne dans l'espace privé de l'application.
- Reprise de lecture et historique enregistrés localement.
- Affiches, titres, résumés, dates et autres métadonnées via TMDB et OMDb, demandés en français auprès de TMDB avec la région France.
- Disponibilités françaises des films via TMDB/JustWatch : sortie cinéma récente, streaming par abonnement, location et achat.
- Le bouton principal d'une fiche ne propose la lecture que lorsqu'un fichier Drive ou hors ligne est réellement disponible ; sinon il indique le type de disponibilité externe.
- Interface française, y compris les réglages, le lecteur, les notifications, les téléchargements et les messages d'erreur à partir de la version 1.0.3.
- Recherche, listes personnelles, saisons et épisodes.
- Mode image dans l'image pris en charge par le lecteur.

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

Movynex n'intègre aucun identifiant OAuth dans le dépôt ou dans l'APK. Le Client ID et le Client Secret doivent être transmis de façon privée aux utilisateurs autorisés, puis saisis localement dans l'application.

La configuration Google Cloud utilisée par la famille est la suivante :

- projet : **Movynex Personal** ;
- application OAuth : **Movynex** ;
- client : **Movynex Android** ;
- type de client : application de bureau ;
- scope : `https://www.googleapis.com/auth/drive.readonly` ;
- URI de redirection : `http://127.0.0.1:53682/` ;
- audience : externe, statut **In production**.

Pages publiques associées à l'écran de consentement :

- [Accueil Movynex](https://sites.google.com/view/movynex/home)
- [Politique de confidentialité](https://sites.google.com/view/movynex/privacy-policy)
- [Conditions d'utilisation](https://sites.google.com/view/movynex/terms-of-service)

### Première connexion

1. Ouvrir les paramètres Google Drive de Movynex.
2. Saisir le Client ID, le Client Secret et `http://127.0.0.1:53682/`.
3. Vérifier que le scope affiché est `https://www.googleapis.com/auth/drive.readonly`.
4. Appuyer sur **Sign in**, choisir le compte Google et accepter l'accès en lecture seule.
5. Le navigateur finit sur une adresse `127.0.0.1` qui peut afficher « site inaccessible » : ce comportement est attendu.
6. Copier l'adresse complète depuis la barre d'adresse du navigateur.
7. Revenir dans Movynex, choisir **Enter authorization code?**, coller l'adresse complète puis valider.
8. Sélectionner les dossiers de films et de séries, puis lancer l'indexation.

Le statut OAuth en production évite l'expiration automatique au bout de sept jours propre au mode de test. Comme l'application familiale n'est pas validée publiquement par Google, un avertissement « application non validée » peut apparaître lors de la première connexion et le projet reste soumis à une limite de 100 utilisateurs OAuth.

## Installation et mises à jour

Une nouvelle APK doit conserver à la fois :

- le package `com.cursedcrew.movynex` ;
- la signature Cursed Crew.

Ces deux éléments permettent d'installer une mise à jour par-dessus la version existante sans effacer la connexion Google Drive, les dossiers sélectionnés, l'historique ou les téléchargements. Ne pas désinstaller l'application avant une mise à jour, car une désinstallation supprime ses données locales.

## Développement

### Prérequis

- JDK 17
- Android Studio récent
- Android SDK 36
- Git

### Installation locale

```bash
git clone https://github.com/Oks2199/zplex-android.git
cd zplex-android
```

Créer ou compléter `local.properties` sans le committer :

```properties
sdk.dir=C:/chemin/vers/Android/Sdk
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

## Intégration continue

Le workflow [`.github/workflows/android.yml`](.github/workflows/android.yml) :

- utilise Java 17 ;
- exécute les tests unitaires debug ;
- construit les APK debug par architecture ;
- construit une APK release ARM64 signée lors des exécutions hors pull request ;
- récupère TMDB, OMDb et la signature depuis les secrets GitHub.

Un push sur `main` déclenche automatiquement ce workflow. Une pull request vers `main` exécute les tests et construit le debug, mais ignore volontairement la release signée. Un lancement manuel ne teste que le contenu déjà poussé sur la branche distante sélectionnée : il n'inclut jamais les modifications non committées du poste local. Movynex 1.0.3 a été construite et vérifiée par [GitHub Actions n°14](https://github.com/Oks2199/zplex-android/actions/runs/34718793281), puis livrée dans `H:\Downloads\Movynex-1.0.3.apk`.

## Confidentialité et sécurité

- L'accès Google Drive est limité à la lecture : Movynex ne peut ni modifier ni supprimer les fichiers Drive.
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
