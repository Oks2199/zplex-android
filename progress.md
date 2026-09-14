# Movynex — état d'avancement

Dernière mise à jour : 14 septembre 2026

## Résumé

Movynex 1.0.6 (`versionCode = 10`) est intégrée à `main`, construite, signée, vérifiée et livrée. L'APK finale, basée sur le commit de fusion `fdf4e01`, se trouve dans `H:\Downloads\Movynex-1.0.6.apk`. Cette version regroupe la séparation Bibliothèque/Ma liste, la navigation à trois destinations, l'harmonisation des affiches, la restructuration complète de l'accueil et la nouvelle bannière verticale inspirée de Netflix. Le 14 septembre 2026, l'utilisateur a confirmé que la mise à jour installée fonctionne correctement.

La 1.0.5 livre l'amélioration des séries intégrée au commit `7447e33` : exploration complète des saisons et épisodes, plateformes françaises par série et saison, résumé des épisodes disponibles et états explicites pour chaque épisode.

La pull request n°1 a validé la 1.0.4 avec GitHub Actions n°15 avant son intégration à `main`. GitHub Actions n°16 a ensuite réussi les tests unitaires, l'APK debug et la release ARM64 signée. L'APK finale a été contrôlée puis copiée dans `H:\Downloads`.

La 1.0.4 réduit le monogramme M de l'icône à 85 % de sa taille précédente afin qu'il ne touche plus les limites du masque rond Samsung. Les 15 ressources release, debug et monochrome des cinq densités ont été mises à jour et intégrées à `main`.

La 1.0.4 ajoute les disponibilités françaises des films. Les fiches récupèrent auprès de TMDB les sorties cinéma et les offres JustWatch de streaming, location et achat, avec des caches respectifs de 30 jours et 24 heures. Une section « Où voir ce film en France ? » détaille ces informations. Le bouton principal conserve `Regarder` ou `Continuer la lecture` uniquement lorsqu'un fichier Drive ou hors ligne est détecté ; sinon il indique cinéma, plateforme, location, achat ou indisponibilité. Les téléchargements locaux restent lisibles sans connexion Drive.

La séparation de « Bibliothèque » et « À voir » n'avait pas été incluse dans la 1.0.4, conformément à la correction explicite de l'utilisateur : le schéma de `zplex_db.db`, la navigation inférieure et le fonctionnement historique de `Ma liste` étaient alors restés inchangés.

Cette décision a évolué le 13 septembre 2026 : l'utilisateur a validé une vraie séparation entre **Bibliothèque** et **Ma liste**, tout en conservant seulement trois boutons dans la barre inférieure. L'implémentation est livrée dans Movynex 1.0.6.

## Bibliothèque et Ma liste — livrées dans Movynex 1.0.6

- Aucun changement de schéma Room : `fileId` distingue déjà une ligne de Bibliothèque d'une ligne de Ma liste.
- L'écran Bibliothèque affiche uniquement les médias détectés sur Drive ; le nouvel écran Ma liste affiche uniquement les ajouts manuels absents du Drive.
- Une indexation Drive transforme automatiquement une ligne de Ma liste en ligne de Bibliothèque en lui attribuant son `fileId`.
- Le nettoyage de l'indexeur ignore désormais Ma liste, au lieu de pouvoir supprimer ses lignes sans `fileId`.
- Le bouton des fiches distingue « Ajouter à ma liste », « Dans ma liste » et « Dans la bibliothèque » ; ce dernier état est protégé contre une suppression accidentelle.
- La barre inférieure contient Accueil, Bibliothèque et Ma liste. La loupe de recherche est placée dans les barres supérieures et Recherche possède maintenant une flèche retour.
- La déconnexion conserve la dernière Bibliothèque connue et Ma liste au lieu de transformer tous les médias Drive en éléments de Ma liste.
- Tests unitaires ajoutés pour les trois états, le passage automatique vers la Bibliothèque et la protection du nettoyage Drive.
- Les affiches TMDB sont harmonisées au ratio `2:3` avec `centerCrop`. Cela corrige les hauteurs variables dans « Continuer la lecture » et dans le composant partagé par l'accueil, la recherche, l'exploration, la Bibliothèque, Ma liste, les recommandations et la filmographie, tout en évitant l'étirement des rares affiches atypiques sur les fiches et saisons.
- Contrôles locaux réussis : compilation AAPT2 35.0.0, compilation Kotlin de la logique pure et `git diff --check`.
- Gradle local s'arrête avant la configuration sur la limite connue `Unable to establish loopback connection` avec le JDK 17 et `--stacktrace`.
- Validation distante réussie par la [pull request n°3](https://github.com/Oks2199/zplex-android/pull/3) et GitHub Actions n°21, puis intégration dans `main` et livraison avec la release 1.0.6.

## Restructuration de l'accueil — livrée dans Movynex 1.0.6

- Ordre dynamique implémenté : bannière, Continuer la lecture, Sur votre Drive, Ma liste, À découvrir, Tendances de la semaine, Actuellement au cinéma, puis Disponibles en streaming en France.
- La bannière privilégie les ajouts Drive récents disposant d'une affiche TMDB. Elle adopte maintenant une grande carte verticale `2:3` inspirée de Netflix : affiche plein cadre, dégradé inférieur, titre, type, année, note et origine superposés, puis bouton « Voir la fiche ». L'ancien fond horizontal flouté et la carte imbriquée sont supprimés. Le libellé indique « Sur votre Drive », « Au cinéma » ou « Sur &lt;plateforme&gt; · JustWatch » selon la source.
- Continuer la lecture exclut les progressions nulles ou terminées, les fichiers hors ligne supprimés et les contenus Drive qui ne sont plus indexés. Le média correspondant disparaît de Sur votre Drive afin d'éviter le doublon.
- Les nouveaux historiques enregistrent le `fileId` exact et l'état hors ligne du film ou de l'épisode. `zplex_db.db` passe de la version 2 à la version 3 avec une migration non destructive préservant les réglages et l'historique existants.
- Sur votre Drive fusionne films et séries par `modifiedTime`. Ma liste est masquée lorsqu'elle est vide et exclut aussi les téléchargements hors ligne encore présents.
- L'ancien faux classement streaming fondé sur `tv/on_the_air` est remplacé par les découvertes TMDB films et séries filtrées sur `watch_region=FR` et `flatrate|free|ads`, fusionnées par popularité avec attribution JustWatch.
- Les sorties utilisent `movie/now_playing` avec la région `FR`. Les tendances utilisent la fenêtre TMDB `week`, mondiale, avec les métadonnées françaises.
- Tests unitaires ajoutés pour les règles de progression et de source lisible. AAPT2 35.0.0 compile toutes les ressources, y compris la nouvelle bannière, et la logique pure compile avec Kotlin.
- La tentative Gradle locale avec le runtime Android Studio et `--stacktrace` atteint la limite connue `Unable to establish loopback connection` avant la configuration.
- La première exécution de la pull request n°3 a détecté l'identifiant manquant du graphe Navigation pour les nouvelles actions globales. Le commit `a106c1b` a corrigé ce point ; GitHub Actions n°21 a ensuite validé les tests unitaires, Room/KSP et l'APK debug.
- Après fusion, [GitHub Actions n°22](https://github.com/Oks2199/zplex-android/actions/runs/34784568126) a réussi les tests, l'APK debug et la release ARM64 signée.

## Movynex 1.0.6

- Version construite : **Movynex 1.0.6**, `versionCode = 10` et `versionName = 1.0.6`.
- Pull request de validation : [n°3](https://github.com/Oks2199/zplex-android/pull/3).
- Commit applicatif livré sur `origin/main` : `fdf4e01`.
- Validation debug réussie après correctif : [GitHub Actions n°21](https://github.com/Oks2199/zplex-android/actions/runs/34784323576).
- Tests, APK debug et release ARM64 signée réussis : [GitHub Actions n°22](https://github.com/Oks2199/zplex-android/actions/runs/34784568126).
- APK vérifiée : package `com.cursedcrew.movynex`, libellé Movynex, version 1.0.6/code 10 et architecture exclusive `arm64-v8a`.
- Signature Cursed Crew vérifiée avec l'empreinte de certificat attendue ; signature APK v2 et alignement ZIP valides.
- Les clés TMDB et OMDb sont présentes dans la release, sans que leurs valeurs aient été affichées.
- Taille : `39 328 401` octets.
- Empreinte SHA-256 : `A7ECD57F9DDA9D0AA9A8737ED0BE540405F37BC8839EAB70384105E70951C971`.
- Artefact vérifié : `F:\Developpement\zplex-android\dist\run-34784568126-release\app-arm64-v8a-release.apk`.
- APK livrée : `H:\Downloads\Movynex-1.0.6.apk`, avec exactement la même empreinte.
- La migration `MIGRATION_2_3` ajoute uniquement les informations de source aux historiques de lecture et préserve les données existantes ; le package, la signature, les réglages, OAuth et les formats des dossiers Drive restent compatibles avec une mise à jour par-dessus la 1.0.5.
- Installation et fonctionnement général confirmés par l'utilisateur le 14 septembre 2026.

## Movynex 1.0.5

- Version construite : **Movynex 1.0.5**, `versionCode = 9` et `versionName = 1.0.5`.
- Commit applicatif livré sur `origin/main` : `df231c0`.
- Validation technique préalable : [pull request n°2](https://github.com/Oks2199/zplex-android/pull/2) et [GitHub Actions n°17](https://github.com/Oks2199/zplex-android/actions/runs/34757127421).
- Tests, APK debug et release ARM64 signée réussis : [GitHub Actions n°19](https://github.com/Oks2199/zplex-android/actions/runs/34760256778).
- APK vérifiée : package `com.cursedcrew.movynex`, libellé Movynex, version 1.0.5/code 9 et architecture exclusive `arm64-v8a`.
- Signature Cursed Crew vérifiée avec l'empreinte de certificat attendue ; signature APK v2 et alignement ZIP valides.
- Les clés TMDB et OMDb sont présentes dans la release, sans que leurs valeurs aient été affichées.
- Taille : `39 285 591` octets.
- Empreinte SHA-256 : `BD0742C08C5FA14A5C3B6419A8DB11F1E7D259F215F3D6F4E0B0A3C651DD6CC8`.
- Artefact vérifié : `F:\Developpement\zplex-android\dist\run-34760256778-release\app-arm64-v8a-release.apk`.
- APK livrée : `H:\Downloads\Movynex-1.0.5.apk`, avec exactement la même empreinte.
- Aucun schéma Room, stockage DataStore, réglage OAuth ou format de dossier Drive n'a été modifié ; l'installation peut remplacer la 1.0.4 sans désinstallation.

## Amélioration des séries livrée dans la 1.0.5

- Commit d'implémentation : `b817a6b` ; commit d'intégration dans `main` : `7447e33`.
- Pull request de validation [n°2](https://github.com/Oks2199/zplex-android/pull/2), fusionnée le 13 septembre 2026.
- Le bouton de la fiche série devient « Saisons et épisodes » et conserve l'accès complet aux saisons et épisodes, avec ou sans fichier Drive.
- Les plateformes françaises sont affichées au niveau général de la série puis au niveau précis de la saison consultée, avec attribution JustWatch.
- Les nouvelles entrées de plateformes sont conservées 24 heures dans le cache et sont couvertes par la remise à zéro existante.
- La saison ouverte affiche le nombre d'épisodes présents sur le Drive et téléchargés sans analyser toutes les autres saisons.
- Chaque épisode indique « Téléchargé », « Sur le Drive » ou « Pas sur le Drive ». Sans fichier, un toucher ouvre les informations de l'épisode sans imposer de connexion Drive.
- Aucun schéma Room, stockage DataStore, réglage OAuth ou format de dossier Drive n'est modifié.
- Tests ajoutés pour les plateformes françaises et les trois états des épisodes.
- Validation locale : ressources compilées par AAPT2 35.0.0 et `git diff --check` réussi. Gradle reste bloqué avant configuration par la limite Windows connue.
- Validation distante réussie : [GitHub Actions n°17](https://github.com/Oks2199/zplex-android/actions/runs/34757127421) a exécuté les tests unitaires, compilé l'APK debug ARM64 et téléversé l'artefact.
- Validation après intégration réussie : [GitHub Actions n°18](https://github.com/Oks2199/zplex-android/actions/runs/34759881898) a exécuté les tests, construit l'APK debug et produit une release ARM64 signée.
- Cette construction intermédiaire n°18 portait encore la version 1.0.4/code 8 et n'a pas été livrée.
- La version finale 1.0.5/code 9 a été construite et livrée après cette validation.

## Movynex 1.0.4

- Version construite : **Movynex 1.0.4**, `versionCode = 8` et `versionName = 1.0.4`.
- Code applicatif publié sur `origin/main` au commit `16f1c55` après validation de la [pull request n°1](https://github.com/Oks2199/zplex-android/pull/1).
- Validation debug réussie : [GitHub Actions n°15](https://github.com/Oks2199/zplex-android/actions/runs/34755058380).
- Tests, APK debug et release signée réussis : [GitHub Actions n°16](https://github.com/Oks2199/zplex-android/actions/runs/34755317919).
- APK vérifiée : package `com.cursedcrew.movynex`, libellé Movynex et architecture exclusive `arm64-v8a`.
- Signature Cursed Crew vérifiée avec l'empreinte de certificat attendue.
- Les clés TMDB et OMDb sont présentes dans la release, sans que leurs valeurs aient été affichées.
- Taille : `39 281 563` octets.
- Empreinte SHA-256 de l'APK : `B04F1809D6D715D79A2013C0F907B88923902E7262584DC5D71CC9F63D2E01D7`.
- Artefact vérifié : `F:\Developpement\zplex-android\dist\run-34755317919-release\app-arm64-v8a-release.apk`.
- APK livrée : `H:\Downloads\Movynex-1.0.4.apk`, avec exactement la même empreinte.
- Aucun schéma de base de données, clé DataStore ou stockage persistant n'a été renommé ; la mise à jour peut être installée par-dessus la 1.0.3 sans désinstallation.

## Correction française livrée dans Movynex 1.0.3

- L'écran Recherche de la version 1.0.2 affiche encore `Search for a movie, tv show…` dans son champ de saisie.
- La version 1.0.3 traduit ce champ en `Rechercher un film ou une série…`.
- Les messages « aucun résultat », absence de connexion et erreur réseau du même écran ont également été traduits.
- Le passage français a été étendu aux autres textes visibles repérés dans l'interface, les erreurs, les notifications et les tâches de fond.
- Les filtres de l'écran Discover ne comparent plus les libellés anglais `Movies` et `TV Shows` et ne convertissent plus le texte de tri avec `SortBy.valueOf()` ; les genres et tris traduits sont associés à leurs identifiants ou enums stables.
- Le 12 septembre 2026, l'utilisateur a décidé de ne pas implémenter le sélecteur Français / English / Système pour le moment ; Movynex reste une application familiale en français.
- La compilation AAPT2 de toutes les ressources Android réussit avec la version 35.0.0 et `git diff --check` ne relève aucune erreur.
- Avec le JDK 17, les tests et l'assemblage debug locaux restent bloqués avant la configuration du projet par `Unable to establish loopback connection`, limite connue de l'environnement Codex.
- GitHub Actions n°12 a détecté un import `R` manquant dans `CastViewModel`, corrigé par le commit `4643428`.
- GitHub Actions n°13 a ensuite détecté une attente anglaise obsolète dans `ConverterUtilsTest`, corrigée par le commit `2fae67b`.
- GitHub Actions n°14 a réussi les tests unitaires, l'assemblage debug et la release ARM64 signée.

## Movynex 1.0.3

- Version construite : **Movynex 1.0.3**, `versionCode = 7` et `versionName = 1.0.3`.
- Code applicatif publié sur `origin/main` au commit `2fae67b`.
- Workflow réussi : [GitHub Actions n°14](https://github.com/Oks2199/zplex-android/actions/runs/34718793281).
- APK vérifiée : package `com.cursedcrew.movynex`, libellé Movynex et architecture exclusive `arm64-v8a`.
- Signature Cursed Crew vérifiée avec l'empreinte de certificat attendue.
- Les clés TMDB et OMDb sont présentes dans la release, sans que leurs valeurs aient été affichées.
- Empreinte SHA-256 de l'APK : `58A315F0930B2491DF9F81DAA26406484E492240DBC2ADE486B9B3E214BD26A3`.
- Artefact vérifié : `F:\Developpement\zplex-android\dist\run-34718793281-release\app-arm64-v8a-release.apk`.
- APK livrée : `H:\Downloads\Movynex-1.0.3.apk`, avec exactement la même empreinte.
- Aucun schéma de base de données, clé DataStore ou stockage persistant n'a été renommé ; la mise à jour peut être installée par-dessus la 1.0.2 sans désinstallation.

## Movynex 1.0.2

- Version construite : **Movynex 1.0.2**, `versionCode = 6` et `versionName = 1.0.2`.
- Les requêtes TMDB utilisent maintenant la langue `fr-FR` et la région `FR`.
- Les principaux libellés visibles des fiches de films et séries, des saisons et des épisodes ont été traduits en français.
- Les dates affichées dans ces écrans utilisent une locale française.
- Le format technique `Season N` reste inchangé pour préserver la correspondance avec les dossiers Google Drive.
- Aucun schéma de base de données ni stockage persistant n'a été modifié.
- Le fichier `strings.xml` traduit a été compilé avec succès par `aapt2` 35.0.0.
- La compilation locale a été tentée, mais Gradle s'arrête avant de configurer le projet avec `Unable to establish loopback connection` dans l'environnement Codex.
- Le code est publié sur `origin/main` au commit `302d08d`.
- GitHub Actions n°11 a réussi : tests unitaires, APK debug et release ARM64 signée.
- L'APK release a été vérifiée : package `com.cursedcrew.movynex`, version 1.0.2/code 6, libellé Movynex, architecture ARM64 et signature Cursed Crew.
- Empreinte SHA-256 : `06F528A76197C0BA38C13A53447B5B94EBD969D2C0D1DAEF4EAE9D0B5BC2B57A`.
- Artefact vérifié : `F:\Developpement\zplex-android\dist\run-34693277079-release\app-arm64-v8a-release.apk`.
- L'APK livrée `H:\Downloads\Movynex-1.0.2.apk` possède exactement la même empreinte que l'artefact vérifié. Elle est installée sur le téléphone et l'utilisateur a confirmé que l'application fonctionne.
- Après installation d'une future APK, les deux films déjà indexés pourront être actualisés par un appui long sur le compteur du cache API dans les réglages, puis en les retirant de la bibliothèque par glissement et en relançant l'analyse des médias. L'historique de lecture reste séparé.

## Release de référence

- Nom : **Movynex 1.0.6**
- `versionCode` : `10`
- `versionName` : `1.0.6`
- Package : `com.cursedcrew.movynex`
- Libellé Android : `Movynex`
- Architecture livrée : `arm64-v8a`
- Signature : Cursed Crew
- Empreinte SHA-256 du certificat : `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- Empreinte SHA-256 de l'APK : `A7ECD57F9DDA9D0AA9A8737ED0BE540405F37BC8839EAB70384105E70951C971`
- Workflow : [GitHub Actions n°22](https://github.com/Oks2199/zplex-android/actions/runs/34784568126), terminé avec succès
- Emplacement livré : `H:\Downloads\Movynex-1.0.6.apk`

## Fonctionnement validé

- Connexion à Google Drive opérationnelle.
- Sélection et indexation des dossiers Drive opérationnelles.
- Ouverture et lecture d'un film via le lecteur OAuth direct opérationnelles.
- Téléchargement hors ligne conservé dans le stockage privé de l'application.
- Plantage `NumberFormatException` avec la locale française corrigé.
- Tests unitaires et compilations debug/release de la 1.0.6 réussis dans GitHub Actions n°22.
- Package, version, libellé, architecture ARM64, clés API, signature v2 et alignement ZIP de l'APK finale contrôlés après téléchargement.
- Empreinte SHA-256 identique entre l'artefact vérifié et sa copie dans `H:\Downloads`.
- Le fonctionnement des nouveaux écrans séries de la 1.0.5 a été confirmé par l'utilisateur.
- La navigation Bibliothèque/Ma liste, l'accueil restructuré, la nouvelle bannière et le fonctionnement général de la 1.0.6 ont été validés sur le téléphone par l'utilisateur le 14 septembre 2026.

## Identité Movynex

- Nom public Android et OAuth : **Movynex**.
- Package Android : `com.cursedcrew.movynex`.
- Namespace Kotlin historique conservé en interne : `zechs.zplex`.
- Mot-symbole bleu/violet affiché dans la barre supérieure de l'accueil.
- Icône livrée : grand **M** haute définition sur fond presque noir, réduit à 85 % dans la 1.0.4 pour préserver une marge dans les masques Android.
- Toutes les densités release, debug et monochromes utilisent la nouvelle identité.
- Les anciens visuels ZPlex ont été retirés des variantes debug.
- Les identifiants publics du lecteur, des notifications et des téléchargements ont été renommés pour Movynex.
- Les mentions ZPlex restantes sont limitées aux noms techniques internes et aux crédits du projet d'origine.

## Google Cloud et OAuth

- Projet : **Movynex Personal**.
- Client : **Movynex Android**.
- Client ID et Client Secret existants conservés et absents de Git.
- Scope Google Drive : `https://www.googleapis.com/auth/drive.readonly`.
- URI de redirection : `http://127.0.0.1:53682/`.
- Audience externe, statut **In production** depuis le 11 septembre 2026.
- L'expiration automatique des autorisations au bout de sept jours liée au mode Testing ne s'applique plus aux nouvelles autorisations.
- L'application reste non validée publiquement : un avertissement Google peut apparaître et la limite OAuth affichée est de 100 utilisateurs.
- Domaine OAuth autorisé : `google.com`.
- Site public : `https://sites.google.com/view/movynex/home`.
- Politique de confidentialité : `https://sites.google.com/view/movynex/privacy-policy`.
- Conditions d'utilisation : `https://sites.google.com/view/movynex/terms-of-service`.
- Le titre public Google Sites a été corrigé de **Nothing - Void Lords** vers **Movynex**.

## Git et branches

- `origin` : `https://github.com/Oks2199/zplex-android.git`.
- `upstream` : `https://github.com/ZPlexLabs/zplex-android.git`.
- `main` contient le build applicatif 1.0.6 au commit de fusion `fdf4e01`.
- `codex/watchlist-navigation` conserve la branche ayant servi à la pull request n°3 ; son contenu est intégré dans `main`.
- `codex/oauth-restoration` conserve l'historique de restauration du lecteur OAuth direct.
- `codex/saf-migration` au commit `cd27201` conserve l'expérience SAF abandonnée pour la lecture distante.
- Le dossier `dist/` contient uniquement des artefacts locaux et reste hors de Git.

## Jalons principaux

- `cf609ce` — correction du format décimal avec la locale française.
- `2013477` — identité release Movynex, package, signature et workflow GitHub.
- `c58bcd5` — nettoyage principal de l'identité Movynex.
- `f50c471` — restauration du lecteur Google Drive OAuth direct.
- `5b51e09` — nouvelle icône Movynex et release applicative 1.0.1.
- `031be35` — refonte de la documentation générale Movynex.
- `302d08d` — métadonnées TMDB françaises et préparation de Movynex 1.0.2.
- `7d19a0a` — passage complet de l'interface en français et préparation de Movynex 1.0.3.
- `4643428` — correction de l'import de ressources détectée par GitHub Actions n°12.
- `2fae67b` — mise à jour du test de temps relatif français ; commit applicatif livré en 1.0.3.
- `16f1c55` — disponibilités françaises des films et préparation de Movynex 1.0.4.
- `7447e33` — intégration des disponibilités françaises des séries et des états des épisodes.
- `df231c0` — incrément de version et build applicatif livré en 1.0.5.
- `e6cb8ef` — séparation Bibliothèque/Ma liste, accueil restructuré, affiches `2:3` et préparation de Movynex 1.0.6.
- `a106c1b` — identifiant du graphe Navigation requis par les actions globales.
- `fdf4e01` — fusion de la pull request n°3 et build applicatif livré en 1.0.6.

## Documentation de reprise

- `PROJECT_CONTEXT.md` centralise désormais les informations nécessaires à une nouvelle conversation : dépôt et branches, identité Android, compilation locale et GitHub, clé Cursed Crew, secrets attendus, vérification de l'APK, Google Cloud, OAuth, Google Sites, connexion, stockage, conventions de nommage et décisions techniques.
- La reprise doit commencer par la lecture intégrale et séquentielle de `AGENTS.md`, `PROJECT_CONTEXT.md`, `progress.md`, puis `README.md`, avant toute action sur le projet.
- `LANGUAGE_AUDIT.md` conserve l'audit complet d'une éventuelle sélection Français / English / Système : architecture AndroidX, ressources, langue TMDB, cache, métadonnées persistantes, pièges de logique, plan d'implémentation et checklist de validation. Cette fonctionnalité est différée sans date au profit d'une interface française unique.
- Le 12 septembre 2026, la procédure a été précisée après une reprise de session difficile : l'erreur Gradle `Unable to establish loopback connection` de l'environnement Codex, la validation par pull request, le comportement de `workflow_dispatch` et la séparation entre validation debug et livraison signée sont maintenant documentés explicitement.
- La 1.0.3 a exceptionnellement été poussée directement sur `main` après l'accord de produire une nouvelle mise à jour. Les erreurs détectées par les exécutions n°12 et n°13 confirment qu'une prochaine modification applicative doit d'abord passer par une branche et une pull request lorsque la validation locale est bloquée.
- `README.md`, `AGENTS.md` et `progress.md` renvoient vers ce dossier de reprise.
- Aucune valeur de secret n'est enregistrée dans la documentation.
- La clé Cursed Crew fonctionne dans GitHub Actions, mais aucune copie privée originale du fichier n'a été retrouvée dans le dépôt ou les emplacements locaux parcourus. Une sauvegarde chiffrée externe reste à localiser ou à confirmer.

## Validation restante

- Aucune validation fonctionnelle ne reste à effectuer pour Movynex 1.0.6.
- Ne reprendre le chantier bilingue de `LANGUAGE_AUDIT.md` que si le besoin familial évolue.

## Dette et améliorations futures

- Retirer les journaux `Log.d` qui peuvent contenir le client OAuth, un code d'autorisation ou des jetons avant toute distribution plus large.
- Moderniser progressivement le design de l'interface.
- Conserver la migration SAF uniquement comme expérience séparée tant que la lecture distante n'est pas immédiate.
