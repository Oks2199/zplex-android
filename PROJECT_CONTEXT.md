# Movynex — dossier de reprise

Ce document rassemble les informations nécessaires pour reprendre le projet dans une nouvelle conversation sans recommencer les recherches. Il ne doit contenir aucun mot de passe, jeton, code OAuth ni valeur de clé API.

## Ordre de lecture pour une nouvelle conversation

1. Lire `AGENTS.md` avant toute modification.
2. Lire ce fichier en entier.
3. Lire `progress.md` pour connaître le dernier état livré et les validations restantes.
4. Lire `README.md` en entier pour l'utilisation de l'application et les conventions de nommage des médias.
5. Lire `LANGUAGE_AUDIT.md` avant toute modification liée aux langues, aux ressources, à TMDB ou au cache de métadonnées.
6. Vérifier `git status`, la branche courante et les derniers commits avant d'écrire quoi que ce soit.

Message de départ conseillé dans une nouvelle conversation :

```text
Reprends le projet Movynex dans F:\Developpement\zplex-android. Lis entièrement AGENTS.md, PROJECT_CONTEXT.md, progress.md et README.md avant toute action. Vérifie ensuite l'état Git et résume-moi l'état actuel sans modifier ni reconstruire l'application tant que je ne l'ai pas demandé.
```

## Objectif et périmètre

Movynex est un fork familial privé de ZPlex. L'application parcourt une vidéothèque Google Drive, enrichit les titres avec TMDB et OMDb, puis lit les vidéos avec MPV. Elle est prévue pour deux ou trois utilisateurs connus et n'est pas destinée au Google Play Store.

Le dépôt d'origine reste crédité et la licence MIT doit être conservée. Les noms techniques historiques peuvent rester internes, mais aucun ancien nom ZPlex ne doit être visible dans l'expérience Movynex.

## Dépôt Git

- Dossier de travail Windows : `F:\Developpement\zplex-android`
- Dépôt familial `origin` : `https://github.com/Oks2199/zplex-android.git`
- Dépôt d'origine `upstream` : `https://github.com/ZPlexLabs/zplex-android.git`
- Branche de livraison : `main`
- Release applicative 1.0.1 : commit `5b51e09`
- Release applicative 1.0.2 : commit `302d08d`
- Release applicative 1.0.3 : commit `2fae67b`
- Release applicative 1.0.4 : commit `16f1c55`
- Release applicative 1.0.5 : commit `df231c0`
- Première refonte générale de la documentation : commit `031be35`
- Workflow : `.github/workflows/android.yml`
- Page GitHub Actions : `https://github.com/Oks2199/zplex-android/actions`
- Visibilité GitHub vérifiée : dépôt public et fork du projet d'origine

Un push applicatif sur `main` déclenche les tests et la construction. Il faut l'accord explicite de l'utilisateur avant ce push. Un commit uniquement documentaire peut utiliser `[skip ci]` afin de ne pas produire inutilement une nouvelle APK. Le dépôt étant public, aucun secret, identifiant privé ou fichier de signature ne doit y être ajouté, même temporairement.

## Identité Android à préserver

- Nom public : **Movynex**
- Package installé : `com.cursedcrew.movynex`
- Namespace Kotlin historique : `zechs.zplex`
- Projet Gradle : `Movynex`
- Version livrée : `versionCode = 9`, `versionName = 1.0.5`
- Android minimum : API 31
- Android cible et compilation : API 36
- Architecture distribuée : `arm64-v8a`
- Variante debug : package `com.cursedcrew.movynex.debug`, suffixe de version `-DEBUG`

Pour toute mise à jour destinée à remplacer l'application installée, conserver impérativement le package et la signature, puis augmenter `versionCode`. Changer le package créerait une seconde application ; changer la clé empêcherait Android d'accepter la mise à jour.

## Clé de signature Cursed Crew

La release Movynex est signée avec la clé **Cursed Crew**, également utilisable pour signer d'autres projets Android. Une même clé peut signer plusieurs packages différents. En revanche, toutes les futures mises à jour de `com.cursedcrew.movynex` doivent continuer à utiliser cette clé précise.

- Format reconstruit dans GitHub Actions : PKCS#12, fichier temporaire `cursedcrew-release.p12`
- Organisation inscrite dans le certificat : **Cursed Crew Studio**
- Empreinte SHA-256 du certificat : `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- La clé privée et ses mots de passe ne sont pas dans Git.
- Aucun fichier `.p12`, `.jks` ou `.keystore` n'est actuellement conservé dans ce dépôt.
- Le workflow reconstitue temporairement la clé depuis les secrets GitHub, construit l'APK, puis l'environnement GitHub est détruit.

Secrets GitHub nécessaires à la signature :

```text
CURSED_CREW_KEYSTORE_BASE64
CURSED_CREW_KEYSTORE_PASSWORD
CURSED_CREW_KEY_ALIAS
CURSED_CREW_KEY_PASSWORD
```

Variables lues par Gradle pour une signature locale :

```text
MOVYNEX_KEYSTORE_PATH
MOVYNEX_KEYSTORE_PASSWORD
MOVYNEX_KEY_ALIAS
MOVYNEX_KEY_PASSWORD
```

Point de sauvegarde important : GitHub ne permet pas de relire la valeur d'un secret après son enregistrement. La construction distante fonctionne actuellement, mais aucune copie privée originale du fichier Cursed Crew n'a été retrouvée dans le dépôt, les autres projets de développement parcourus, les téléchargements ou les emplacements utilisateur courants. Il faut conserver une copie chiffrée hors du dépôt, avec l'alias et les mots de passe dans un gestionnaire de mots de passe. Ne jamais régénérer une nouvelle clé pour une mise à jour de Movynex.

La valeur de l'alias n'est volontairement pas documentée : elle est stockée dans `CURSED_CREW_KEY_ALIAS`. Une nouvelle conversation ne doit ni la deviner ni remplacer le secret si elle n'a pas reçu la valeur de façon privée.

## Clés TMDB et OMDb

Deux clés sont nécessaires pendant la compilation :

```text
TMDB_API_KEY
OMDB_API_KEY
```

En local, elles sont présentes dans `local.properties`, qui est ignoré par Git. Dans GitHub Actions, elles sont enregistrées dans les secrets du dépôt sous les mêmes noms. Ne jamais recopier leurs valeurs dans la documentation, un commit ou une discussion.

## Compilation locale en ligne de commande

Android Studio n'a pas besoin d'être ouvert. Les composants installés sur cette machine sont :

- SDK Android : `C:\Users\Utilisateur\AppData\Local\Android\Sdk`
- plateformes présentes jusqu'à `android-36`
- runtime Java d'Android Studio : `C:\Program Files\Android\Android Studio\jbr`
- Gradle Wrapper : version 8.13
- Android Gradle Plugin : version 8.12.2
- bytecode Java/Kotlin ciblé : Java 17

Le `java` global de Windows pointe encore vers Java 8. Il ne faut donc pas lancer Gradle sans sélectionner d'abord le runtime récent. Dans une nouvelle fenêtre PowerShell :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Set-Location 'F:\Developpement\zplex-android'
```

Vérifier que `local.properties` contient, sans le committer :

```properties
sdk.dir=C:/Users/Utilisateur/AppData/Local/Android/Sdk
TMDB_API_KEY=<valeur privée>
OMDB_API_KEY=<valeur privée>
```

Tests et APK debug :

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

APK ARM64 debug attendue :

```text
app\build\outputs\apk\debug\app-arm64-v8a-debug.apk
```

Une release locale exige en plus une copie privée de la clé Cursed Crew et les quatre variables `MOVYNEX_*` :

```powershell
.\gradlew.bat clean testDebugUnitTest assembleRelease
```

APK ARM64 release attendue :

```text
app\build\outputs\apk\release\app-arm64-v8a-release.apk
```

### Limite connue de Gradle dans l'environnement Codex Windows

Le 12 septembre 2026, plusieurs lancements avec le runtime Android Studio, avec et sans daemon, puis dans un processus Windows séparé, ont tous échoué avec `Unable to establish loopback connection`. La trace montre un échec dans `java.nio.channels.Selector.open` pendant la connexion au daemon Gradle, avant la configuration et la compilation du projet. Ce résultat ne constitue donc pas une erreur de compilation du code Movynex.

Procédure à suivre dans une nouvelle conversation :

1. Vérifier une fois le diagnostic avec `--stacktrace`.
2. Si la trace échoue encore avant la configuration du projet, ne pas modifier `gradle.properties`, le JDK ou le code applicatif pour contourner cette limite.
3. Effectuer les contrôles statiques encore possibles localement. Pour les ressources Android, `aapt2` se trouve notamment dans `C:\Users\Utilisateur\AppData\Local\Android\Sdk\build-tools\35.0.0\aapt2.exe`.
4. Utiliser ensuite GitHub Actions pour les tests unitaires et la compilation complète.

Le 12 septembre 2026, le nouveau `strings.xml` français a ainsi été compilé avec succès directement par `aapt2` 35.0.0. Les tests Gradle et les assemblages debug/release ont ensuite réussi dans GitHub Actions n°14.

## Construction recommandée avec GitHub Actions

La méthode de référence est le workflow **Android Build & Test** :

1. Vérifier que les six secrets GitHub nécessaires sont configurés.
2. Modifier `versionCode` et `versionName` dans `app/build.gradle.kts` si une nouvelle version est préparée.
3. Exécuter les tests localement lorsque possible.
4. Si la compilation locale est bloquée par l'environnement Codex, pousser les changements sur une branche dédiée et ouvrir une pull request vers `main` pour exécuter les tests et construire l'APK debug. Sur une pull request, les étapes de signature et de release sont volontairement ignorées.
5. Attendre la réussite de cette validation avant toute livraison.
6. Après accord explicite de l'utilisateur, intégrer ou pousser la modification applicative sur `main`. Ce push relance le workflow, cette fois avec la release ARM64 signée.
7. Télécharger l'artefact `movynex-arm64-release-apk`.
8. Extraire `app-arm64-v8a-release.apk`, vérifier son identité et sa signature, puis seulement le renommer pour la livraison.

Le bouton **Run workflow** (`workflow_dispatch`) peut aussi lancer la construction sur une branche distante existante. Il ne transmet pas le contenu du poste local : le commit à tester doit déjà avoir été poussé sur la branche sélectionnée. Comme cette exécution n'est pas une pull request, elle lance également les étapes de release signée.

Le workflow utilise les six secrets suivants :

```text
TMDB_API_KEY
OMDB_API_KEY
CURSED_CREW_KEYSTORE_BASE64
CURSED_CREW_KEYSTORE_PASSWORD
CURSED_CREW_KEY_ALIAS
CURSED_CREW_KEY_PASSWORD
```

## Vérification et livraison d'une APK

Avant livraison, vérifier au minimum :

- package `com.cursedcrew.movynex` ;
- `versionCode` et `versionName` attendus ;
- libellé public `Movynex` ;
- présence exclusive de l'ABI distribuée `arm64-v8a` ;
- présence des clés TMDB et OMDb sans afficher leurs valeurs ;
- signature APK v2 avec le certificat Cursed Crew et son empreinte connue ;
- alignement ZIP valide avec `zipalign` ;
- absence des anciens visuels ZPlex ;
- fonctionnement de la connexion Drive et de la lecture vidéo.

Après la copie dans `H:\Downloads`, recalculer l'empreinte SHA-256 de la copie et vérifier qu'elle est strictement identique à celle de l'artefact contrôlé.

Pour calculer l'empreinte du fichier sous PowerShell :

```powershell
Get-FileHash -Algorithm SHA256 'chemin\vers\Movynex.apk'
```

Release 1.0.1 vérifiée :

- APK livrée : `H:\Downloads\Movynex-1.0.1.apk`
- Copie locale de travail : `F:\Developpement\zplex-android\dist\final-run-10\app-arm64-v8a-release.apk`
- Taille : `39 220 967` octets
- SHA-256 de l'APK : `647ADB0B00197E314AC4498C48B59EB0F500CFCAFD3788D444F58CC1D3F2A002`
- Workflow réussi : `https://github.com/Oks2199/zplex-android/actions/runs/34603761277`

Release 1.0.2 vérifiée et livrée :

- Commit : `302d08d`
- APK livrée : `H:\Downloads\Movynex-1.0.2.apk`
- Copie locale de l'artefact : `F:\Developpement\zplex-android\dist\run-34693277079-release\app-arm64-v8a-release.apk`
- Taille : `39 229 703` octets
- SHA-256 de l'APK : `06F528A76197C0BA38C13A53447B5B94EBD969D2C0D1DAEF4EAE9D0B5BC2B57A`
- Package : `com.cursedcrew.movynex`
- Version : `versionCode = 6`, `versionName = 1.0.2`
- Libellé : `Movynex`
- Architecture : `arm64-v8a`
- Signature : certificat Cursed Crew attendu, empreinte `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- Tests unitaires, APK debug et release signée réussis : `https://github.com/Oks2199/zplex-android/actions/runs/34693277079`
- Les six secrets GitHub requis ont été confirmés comme configurés, sans lire ni afficher leurs valeurs.

Release 1.0.3 vérifiée et livrée :

- Commit applicatif livré : `2fae67b`
- APK livrée : `H:\Downloads\Movynex-1.0.3.apk`
- Copie locale de l'artefact : `F:\Developpement\zplex-android\dist\run-34718793281-release\app-arm64-v8a-release.apk`
- Taille : `39 270 935` octets
- SHA-256 de l'APK : `58A315F0930B2491DF9F81DAA26406484E492240DBC2ADE486B9B3E214BD26A3`
- Package : `com.cursedcrew.movynex`
- Version : `versionCode = 7`, `versionName = 1.0.3`
- Libellé : `Movynex`
- Architecture exclusive : `arm64-v8a`
- Signature : certificat Cursed Crew attendu, empreinte `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- Les clés TMDB et OMDb sont bien présentes dans la release ; leurs valeurs n'ont pas été affichées.
- Tests unitaires, APK debug et release signée réussis : `https://github.com/Oks2199/zplex-android/actions/runs/34718793281`
- La copie livrée possède exactement la même empreinte SHA-256 que l'artefact vérifié.

Release 1.0.4 vérifiée et livrée :

- Commit applicatif livré : `16f1c55`
- Pull request de validation : [n°1](https://github.com/Oks2199/zplex-android/pull/1), fusionnée après réussite de GitHub Actions n°15
- APK livrée : `H:\Downloads\Movynex-1.0.4.apk`
- Copie locale de l'artefact : `F:\Developpement\zplex-android\dist\run-34755317919-release\app-arm64-v8a-release.apk`
- Taille : `39 281 563` octets
- SHA-256 de l'APK : `B04F1809D6D715D79A2013C0F907B88923902E7262584DC5D71CC9F63D2E01D7`
- Package : `com.cursedcrew.movynex`
- Version : `versionCode = 8`, `versionName = 1.0.4`
- Libellé : `Movynex`
- Architecture exclusive : `arm64-v8a`
- Signature : certificat Cursed Crew attendu, empreinte `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- Les clés TMDB et OMDb sont bien présentes dans la release ; leurs valeurs n'ont pas été affichées.
- Tests unitaires, APK debug et release signée réussis : `https://github.com/Oks2199/zplex-android/actions/runs/34755317919`
- La copie livrée possède exactement la même empreinte SHA-256 que l'artefact vérifié.

Release 1.0.5 vérifiée et livrée :

- Commit applicatif livré : `df231c0`
- Pull request de validation technique : [n°2](https://github.com/Oks2199/zplex-android/pull/2), fusionnée au commit `7447e33`
- APK livrée : `H:\Downloads\Movynex-1.0.5.apk`
- Copie locale de l'artefact : `F:\Developpement\zplex-android\dist\run-34760256778-release\app-arm64-v8a-release.apk`
- Taille : `39 285 591` octets
- SHA-256 de l'APK : `BD0742C08C5FA14A5C3B6419A8DB11F1E7D259F215F3D6F4E0B0A3C651DD6CC8`
- Package : `com.cursedcrew.movynex`
- Version : `versionCode = 9`, `versionName = 1.0.5`
- Libellé : `Movynex`
- Architecture exclusive : `arm64-v8a`
- Signature : certificat Cursed Crew attendu, empreinte `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- Les clés TMDB et OMDb sont bien présentes dans la release ; leurs valeurs n'ont pas été affichées.
- Tests unitaires, APK debug et release signée réussis : `https://github.com/Oks2199/zplex-android/actions/runs/34760256778`
- La copie livrée possède exactement la même empreinte SHA-256 que l'artefact vérifié.

Le dossier `dist/` est local, non versionné et ne doit pas être ajouté à Git.

## Google Cloud et OAuth

Configuration à conserver :

- Projet Google Cloud : **Movynex Personal**
- Marque/application OAuth : **Movynex**
- Client OAuth : **Movynex Android**
- Type de client : application de bureau
- Google Drive API : activée
- Audience : externe
- Statut de publication : **In production**
- Scope : `https://www.googleapis.com/auth/drive.readonly`
- URI de redirection : `http://127.0.0.1:53682/`
- Domaine autorisé pour Google Sites : `google.com`

Liens utiles :

- Console Google Cloud : `https://console.cloud.google.com/`
- Configuration OAuth : **Google Auth Platform** → Branding, Audience, Clients et Data Access
- Éditeur Google Sites utilisé : `https://sites.google.com/u/0/?ec=wgc-sites-%5Bmodule%5D-goto`
- Documentation officielle sur l'audience : `https://support.google.com/cloud/answer/15549945`

Le statut **In production** évite l'expiration automatique des jetons après sept jours qui s'applique au mode Testing avec un scope Drive. Une autorisation créée auparavant en mode Testing peut encore expirer ; il suffit alors de reconnecter le compte une fois. L'application n'étant pas vérifiée publiquement, Google peut afficher un avertissement et applique un plafond de 100 nouveaux utilisateurs, ce qui convient à l'usage familial prévu.

Le projet et le client OAuth existants ont été renommés, pas remplacés. Révoquer l'ancien accès dans le compte Google révoquerait donc aussi l'autorisation utilisée par Movynex et imposerait une reconnexion. Ne le faire que pour déconnecter volontairement l'application.

## Site Google Sites

Le site public créé pour l'identité et les informations OAuth est :

- Accueil : `https://sites.google.com/view/movynex/home`
- Politique de confidentialité : `https://sites.google.com/view/movynex/privacy-policy`
- Conditions d'utilisation : `https://sites.google.com/view/movynex/terms-of-service`

Le titre public est **Movynex**. Il a remplacé l'ancien titre **Nothing - Void Lords**. Ces pages et leurs liens doivent rester publiquement accessibles tant qu'elles sont déclarées dans Google Auth Platform.

## Procédure de connexion Google dans l'application

1. Ouvrir les paramètres Google Drive de Movynex.
2. Saisir localement le Client ID et le Client Secret transmis en privé.
3. Utiliser `http://127.0.0.1:53682/` comme URI de redirection.
4. Vérifier le scope de lecture seule.
5. Appuyer sur **Se connecter** et autoriser l'accès Google Drive.
6. À la fin, la page `127.0.0.1` peut être inaccessible : c'est attendu, aucun serveur local n'écoute sur le téléphone.
7. Copier l'URL complète affichée dans la barre d'adresse.
8. Revenir dans Movynex, choisir **Saisir le code d'autorisation ?**, coller l'URL complète et valider.
9. Sélectionner les dossiers Films et Séries puis lancer l'indexation.

Les identifiants et jetons sont conservés dans l'espace privé de l'application. Une mise à jour avec le même package et la même signature les conserve ; une désinstallation les efface.

## Accès Drive et stockage

- Movynex demande uniquement `drive.readonly` : elle peut lister et télécharger les fichiers, mais pas les modifier ni les supprimer sur Drive.
- Les téléchargements hors ligne sont enregistrés dans le stockage interne privé, dans `movynex-downloads`.
- Désinstaller l'application supprime ses réglages, bases locales et téléchargements.
- Les vidéos distantes doivent continuer à être lues par le lecteur OAuth direct.

## Conventions de nommage des médias

Films, directement dans le dossier choisi :

```text
Titre (Année) [TMDB_ID].mkv
Titre (Année) [TMDB_ID].mp4
```

Séries :

```text
Nom de la série (Année) [TMDB_ID]\Season 1\Nom - S01E01 - Titre.mkv
```

Les extensions de films reconnues sont actuellement `.mkv` et `.mp4` en minuscules. Un épisode doit contenir un motif `SxxExx`. Les fichiers doivent se trouver directement dans leur dossier de film ou de saison respectif.

## Identité graphique

- Icône : grand monogramme **M** bleu/violet sur fond `#15171D`
- Premier plan des icônes release : `app/src/main/res/mipmap-*/ic_launcher_foreground.png`
- Icônes debug et monochromes : variantes `ic_launcher_debug*`
- Mot-symbole de l'accueil : `app/src/main/res/drawable-nodpi/movynex_wordmark.png`
- L'accueil affiche le mot-symbole ; les autres pages gardent leurs titres fonctionnels.

## Historique technique à connaître

- `cf609ce` corrige le `NumberFormatException` provoqué par les nombres décimaux avec une virgule sur un téléphone en français.
- `2013477` crée l'identité de release Movynex, le package et la signature.
- `c58bcd5` termine le nettoyage principal des éléments visibles ZPlex.
- `f50c471` restaure le lecteur OAuth direct qui démarre correctement les films.
- `5b51e09` ajoute l'icône au grand M et correspond au code de la release 1.0.1.
- `031be35` remet la documentation générale à jour sans reconstruire l'APK.

Branches de sauvegarde :

- `codex/oauth-restoration` conserve la restauration du lecteur direct.
- `codex/saf-migration`, commit `cd27201`, conserve l'expérience avec le sélecteur Android SAF.

La migration SAF a été abandonnée pour la release : le fournisseur Google Drive pouvait charger presque tout un MKV avant de permettre les recherches demandées par MPV, ce qui produisait plusieurs minutes d'écran noir. Ne pas la fusionner dans `main` sans une nouvelle solution testée sur le téléphone.

## Dette connue et précautions

- Des appels `Log.d` historiques dans `SessionManager.kt` et `DriveRepository.kt` peuvent exposer le Client ID, des codes ou des jetons OAuth dans les journaux de diagnostic. Les retirer avant toute diffusion plus large que la famille.
- Ne jamais afficher ou committer les valeurs de `local.properties`, des secrets GitHub ou des variables de signature.
- Ne pas modifier le package, la signature, les clés de stockage ou les bases locales sans migration.
- Ne pas annoncer une APK comme livrée ou testée sans avoir vérifié l'artefact correspondant.
- Ne pas remplacer le lecteur OAuth direct par SAF sans validation réelle du démarrage, de l'avance rapide et de la reprise sur une vidéo Drive.

## État à reprendre

Movynex 1.0.5 est publiée sur `origin/main` au commit applicatif `df231c0` et son APK vérifiée est livrée dans `H:\Downloads\Movynex-1.0.5.apk`. Elle conserve les disponibilités françaises des films et l'icône corrigée de la 1.0.4, puis ajoute les disponibilités françaises des séries et des saisons, le récapitulatif des fichiers par saison et les états « Téléchargé », « Sur le Drive » ou « Pas sur le Drive » pour chaque épisode. L'installation de cette APK sur le téléphone et la validation visuelle finale de ces écrans restent à confirmer par l'utilisateur.

Lors de la livraison 1.0.3, les deux premières exécutions ont détecté des erreurs simples : GitHub Actions n°12 a échoué à cause d'un import `R` manquant dans `CastViewModel`, puis n°13 à cause d'une attente anglaise obsolète dans `ConverterUtilsTest`. Les commits `4643428` et `2fae67b` ont corrigé ces deux points. GitHub Actions n°14 a ensuite réussi l'intégralité des tests, la compilation debug et la release ARM64 signée.

Pour la 1.0.3, la demande de « nouvelle mise à jour » a été interprétée comme l'accord de pousser directement sur `main`, sans passer d'abord par la pull request de validation recommandée lorsque Gradle local est bloqué. Ne pas reproduire cet écart : pour une modification applicative suivante, valider d'abord une branche par pull request, puis intégrer sur `main` seulement après réussite et accord explicite de livraison.

L'artefact de GitHub Actions n°14 a été téléchargé dans `dist/run-34718793281-release/`. Son package, sa version 1.0.3/code 7, son libellé, son architecture ARM64 exclusive, la présence des clés API et sa signature Cursed Crew ont été vérifiés. Son SHA-256 est `58A315F0930B2491DF9F81DAA26406484E492240DBC2ADE486B9B3E214BD26A3`. La copie livrée `H:\Downloads\Movynex-1.0.3.apk` possède exactement la même empreinte. Le 13 septembre 2026, l'utilisateur a confirmé que la 1.0.3 installée fonctionne correctement.

Movynex demande les métadonnées TMDB en français (`fr-FR`) et les sorties pour la région France (`FR`). Le format interne `Season N` des dossiers Google Drive reste volontairement inchangé.

Un audit complet d'une éventuelle sélection de langue est conservé dans `LANGUAGE_AUDIT.md`. Le 12 septembre 2026, l'utilisateur a décidé de ne pas l'implémenter pour le moment, Movynex restant une application familiale en français. L'audit reste une référence si une version bilingue devient utile. Le passage français local a néanmoins supprimé les comparaisons avec les libellés anglais `Movies` et `TV Shows` ainsi que la conversion du texte de tri par `SortBy.valueOf()` dans `BrowseFragment` ; les libellés de genre et de tri viennent désormais des ressources et sont associés à leurs valeurs stables.

Pour actualiser les deux films déjà indexés après installation de la 1.0.4 : faire un appui long sur le compteur du cache API dans les réglages pour le réinitialiser, retirer les deux films de la bibliothèque par glissement, puis relancer l'analyse des médias. L'historique de lecture est stocké séparément.

La compilation AAPT2 de toutes les ressources Android de ce passage français réussit avec la version 35.0.0. `git diff --check` ne relève aucune erreur. La tentative locale des tests et de l'assemblage debug avec le JDK 17 s'arrête avant la configuration du projet sur la limite connue `Unable to establish loopback connection` ; la validation complète a donc été effectuée par GitHub Actions n°14.

Après validation de la 1.0.3, la capture du gestionnaire de fichiers Samsung a montré que le M de l'icône touchait visuellement les limites du masque rond. La 1.0.4 réduit exactement les calques de premier plan existants à 85 %, sans redessiner le monogramme, et les recentre sur leur canevas transparent. Elle concerne `ic_launcher_foreground.png`, `ic_launcher_debug_foreground.png` et `ic_launcher_debug_monochrome.png` dans les cinq dossiers de densité. AAPT2 35.0.0 compile ces ressources avec succès.

La 1.0.4 distingue la lecture réelle des disponibilités externes en France sur les fiches de films. TMDB fournit les dates de sortie françaises et les plateformes issues de JustWatch. Les dates sont mises en cache 30 jours avec la clé `movie_<id>_release_dates_FR`; les plateformes sont mises en cache 24 heures avec la clé `movie_<id>_watch_providers_FR`. Le bouton de remise à zéro du cache efface également ces entrées.

Sur une fiche film, la priorité prévue est : fichier Drive ou hors ligne (`Regarder` / `Continuer la lecture`), sortie cinéma française de type 2 ou 3 datant de 30 jours au plus (`Au cinéma`), plateforme unique (`Sur <plateforme>`), plusieurs plateformes, location, achat, puis indisponibilité. Une section séparée « Où voir ce film en France ? » détaille simultanément cinéma, streaming, location et achat et affiche l'attribution JustWatch avec un lien vers la page TMDB quand elle existe. Les films hors ligne peuvent être lus sans connexion Google Drive. La séparation structurelle entre « Bibliothèque » et « À voir » a été explicitement différée par l'utilisateur : aucune migration de base, aucun nouvel onglet et aucune nouvelle table ne font partie de ce chantier.

Les nouveaux tests unitaires couvrent la fenêtre cinéma, la fusion et le tri des plateformes et la priorité du fichier local/Drive. AAPT2 35.0.0 compile toutes les ressources modifiées. Comme attendu dans cet environnement, Gradle lancé avec le JDK 17 s'arrête avant la configuration du projet sur `Unable to establish loopback connection`. La pull request n°1 a toutefois validé les tests Kotlin et l'APK debug avec GitHub Actions n°15. Après fusion sur `main`, GitHub Actions n°16 a réussi les tests, l'APK debug et la release ARM64 signée. L'artefact final a été vérifié puis livré dans `H:\Downloads\Movynex-1.0.4.apk`.

La sélection Français / English / Système reste différée sans date. `LANGUAGE_AUDIT.md` doit être relu avant de reprendre ce chantier.

Le 13 septembre 2026, l'amélioration des fiches de séries a été implémentée sur la branche `codex/series-availability`, commit `b817a6b`, et validée par la pull request n°2. Le bouton de la fiche série affiche désormais « Saisons et épisodes » et reste toujours consacré à l'exploration complète, même lorsqu'aucun fichier n'est présent dans Movynex. La section de disponibilité générale de la série est placée après ce bouton, puis le raccourci vers la dernière saison.

Les plateformes françaises des séries proviennent de `GET /3/tv/{tv_id}/watch/providers`; celles de la saison consultée proviennent de `GET /3/tv/{tv_id}/season/{season_number}/watch/providers`. Les deux réponses sont filtrées sur `FR`, affichent streaming, location et achat avec l'attribution JustWatch et utilisent des caches de 24 heures nommés respectivement `tv_<id>_watch_providers_FR` et `tv_<id>_season_<numéro>_watch_providers_FR`. La remise à zéro globale du cache les efface déjà sans traitement supplémentaire.

La liste complète des saisons n'analyse pas tous les dossiers Drive à l'avance. Lorsqu'une saison est ouverte, le fonctionnement historique recherche uniquement son dossier `Season N`, associe les fichiers `SxxExx`, puis affiche un résumé exact du nombre d'épisodes présents sur le Drive et téléchargés. Chaque épisode porte désormais l'état « Téléchargé », « Sur le Drive » ou « Pas sur le Drive ». Un épisode sans fichier ouvre sa fiche informative sans exiger de connexion Google Drive ; seuls les épisodes possédant un fichier Drive ou local lancent MPV.

Cette évolution ne modifie ni Room, ni DataStore, ni OAuth, ni le format des dossiers Drive. Les tests ajoutés couvrent le filtrage français, le classement et la déduplication des plateformes, ainsi que les trois états d'un épisode. AAPT2 35.0.0 compile les ressources et `git diff --check` réussit. Gradle local reste bloqué avant configuration sur la limite connue `Unable to establish loopback connection`, mais GitHub Actions n°17 a réussi les tests unitaires et l'APK debug ARM64 sur la pull request n°2 : `https://github.com/Oks2199/zplex-android/actions/runs/34757127421`.

La pull request n°2 a ensuite été fusionnée dans `main` au commit applicatif `7447e33`. GitHub Actions n°18 a réussi les tests unitaires, l'APK debug et la release ARM64 signée : `https://github.com/Oks2199/zplex-android/actions/runs/34759881898`. Cette première construction d'intégration portait encore `versionName = 1.0.4` et `versionCode = 8` et n'a pas été livrée.

Le commit `df231c0` a ensuite préparé Movynex 1.0.5 avec `versionCode = 9`. GitHub Actions n°19 a réussi les tests unitaires, l'APK debug et la release ARM64 signée. L'APK a été vérifiée avec AAPT, `zipalign` et `apksigner`, puis copiée dans `H:\Downloads\Movynex-1.0.5.apk`. L'artefact et la copie livrée partagent l'empreinte SHA-256 `BD0742C08C5FA14A5C3B6419A8DB11F1E7D259F215F3D6F4E0B0A3C651DD6CC8`.
