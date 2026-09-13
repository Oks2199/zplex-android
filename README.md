# Movynex

Movynex est une application Android privée destinée à parcourir et lire une vidéothèque familiale stockée sur Google Drive. Elle indexe les films et séries à partir de conventions de nommage, récupère les métadonnées auprès de TMDB et OMDb, puis utilise MPV pour la lecture.

Le projet est un fork familial de [ZPlex](https://github.com/ZPlexLabs/zplex-android). Il n'est pas destiné à être publié sur le Google Play Store.

Pour reprendre le développement dans une nouvelle conversation, lire intégralement et dans cet ordre [`AGENTS.md`](AGENTS.md), [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md), [`progress.md`](progress.md), puis ce `README.md`, avant toute action sur le projet.

## Version actuelle

- Version du code courant : `1.0.6` (`versionCode = 10`), en cours de validation
- Dernière APK livrée et vérifiée : `1.0.5` (`versionCode = 9`)
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
- Accueil organisé autour du contenu personnel : grande bannière verticale `2:3` inspirée de Netflix et prioritairement issue du Drive, lectures réellement commencées, ajouts Drive récents et Ma liste lorsqu'elle n'est pas vide. Les suggestions séparées affichent ensuite les tendances TMDB de la semaine, les films actuellement au cinéma et les films ou séries disponibles en streaming en France.
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
- Navigation principale à trois destinations : Accueil, Bibliothèque et Ma liste. La recherche est accessible par la loupe des barres supérieures.
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
4. Appuyer sur **Se connecter**, choisir le compte Google et accepter l'accès en lecture seule.
5. Le navigateur finit sur une adresse `127.0.0.1` qui peut afficher « site inaccessible » : ce comportement est attendu.
6. Copier l'adresse complète depuis la barre d'adresse du navigateur.
7. Revenir dans Movynex, choisir **Saisir le code d'autorisation ?**, coller l'adresse complète puis valider.
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

Un push sur `main` déclenche automatiquement ce workflow. Une pull request vers `main` exécute les tests et construit le debug, mais ignore volontairement la release signée. Un lancement manuel ne teste que le contenu déjà poussé sur la branche distante sélectionnée : il n'inclut jamais les modifications non committées du poste local. Movynex 1.0.5 a d'abord été validée par la pull request n°2 et [GitHub Actions n°17](https://github.com/Oks2199/zplex-android/actions/runs/34757127421), puis construite, signée et vérifiée par [GitHub Actions n°19](https://github.com/Oks2199/zplex-android/actions/runs/34760256778). L'APK livrée se trouve dans `H:\Downloads\Movynex-1.0.5.apk`.

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
