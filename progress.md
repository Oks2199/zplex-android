# Movynex — état d'avancement

Dernière mise à jour : 12 septembre 2026

## Résumé

Movynex 1.0.2 est construite, signée et livrée. Le code applicatif correspondant est sur `origin/main` au commit `302d08d`. L'APK finale vérifiée se trouve dans `H:\Downloads\Movynex-1.0.2.apk`.

Movynex 1.0.2 a été installée sur le téléphone et l'utilisateur a confirmé que l'application fonctionne. Un oubli de traduction a ensuite été repéré dans Recherche. La correction complète est maintenant préparée pour Movynex 1.0.3 (`versionCode = 7`).

## Correction locale après installation

- L'écran Recherche de la version 1.0.2 affiche encore `Search for a movie, tv show…` dans son champ de saisie.
- La correction locale traduit ce champ en `Rechercher un film ou une série…`.
- Les messages « aucun résultat », absence de connexion et erreur réseau du même écran ont également été traduits.
- Le passage français a été étendu aux autres textes visibles repérés dans l'interface, les erreurs, les notifications et les tâches de fond.
- Les filtres de l'écran Discover ne comparent plus les libellés anglais `Movies` et `TV Shows` et ne convertissent plus le texte de tri avec `SortBy.valueOf()` ; les genres et tris traduits sont associés à leurs identifiants ou enums stables.
- Le 12 septembre 2026, l'utilisateur a décidé de ne pas implémenter le sélecteur Français / English / Système pour le moment ; Movynex reste une application familiale en français.
- La compilation AAPT2 de toutes les ressources Android réussit avec la version 35.0.0 et `git diff --check` ne relève aucune erreur.
- Avec le JDK 17, les tests et l'assemblage debug locaux restent bloqués avant la configuration du projet par `Unable to establish loopback connection`, limite connue de l'environnement Codex. La validation GitHub n'a pas encore été lancée.
- Cette correction est intégrée à la candidate Movynex 1.0.3 (`versionCode = 7`) ; elle n'est pas encore publiée ni construite.

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

- Nom : **Movynex 1.0.2**
- `versionCode` : `6`
- `versionName` : `1.0.2`
- Package : `com.cursedcrew.movynex`
- Libellé Android : `Movynex`
- Architecture livrée : `arm64-v8a`
- Signature : Cursed Crew
- Empreinte SHA-256 du certificat : `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`
- Empreinte SHA-256 de l'APK : `06F528A76197C0BA38C13A53447B5B94EBD969D2C0D1DAEF4EAE9D0B5BC2B57A`
- Workflow : [GitHub Actions n°11](https://github.com/Oks2199/zplex-android/actions/runs/34693277079), terminé avec succès
- Emplacement livré : `H:\Downloads\Movynex-1.0.2.apk`

## Fonctionnement validé

- Connexion à Google Drive opérationnelle.
- Sélection et indexation des dossiers Drive opérationnelles.
- Ouverture et lecture d'un film via le lecteur OAuth direct opérationnelles.
- Téléchargement hors ligne conservé dans le stockage privé de l'application.
- Plantage `NumberFormatException` avec la locale française corrigé.
- Tests unitaires debug et compilations debug/release réussis dans GitHub Actions n°11.
- Package, version, libellé, architecture et signature de l'APK finale contrôlés après téléchargement.

## Identité Movynex

- Nom public Android et OAuth : **Movynex**.
- Package Android : `com.cursedcrew.movynex`.
- Namespace Kotlin historique conservé en interne : `zechs.zplex`.
- Mot-symbole bleu/violet affiché dans la barre supérieure de l'accueil.
- Icône validée : grand **M** haute définition sur fond presque noir.
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
- `main` contient le build applicatif 1.0.2 au commit `302d08d`.
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

## Documentation de reprise

- `PROJECT_CONTEXT.md` centralise désormais les informations nécessaires à une nouvelle conversation : dépôt et branches, identité Android, compilation locale et GitHub, clé Cursed Crew, secrets attendus, vérification de l'APK, Google Cloud, OAuth, Google Sites, connexion, stockage, conventions de nommage et décisions techniques.
- `LANGUAGE_AUDIT.md` conserve l'audit complet d'une éventuelle sélection Français / English / Système : architecture AndroidX, ressources, langue TMDB, cache, métadonnées persistantes, pièges de logique, plan d'implémentation et checklist de validation. Cette fonctionnalité est différée sans date au profit d'une interface française unique.
- Le 12 septembre 2026, la procédure a été précisée après une reprise de session difficile : l'erreur Gradle `Unable to establish loopback connection` de l'environnement Codex, la validation par pull request, le comportement de `workflow_dispatch` et la séparation entre validation debug et livraison signée sont maintenant documentés explicitement.
- `README.md`, `AGENTS.md` et `progress.md` renvoient vers ce dossier de reprise.
- Aucune valeur de secret n'est enregistrée dans la documentation.
- La clé Cursed Crew fonctionne dans GitHub Actions, mais aucune copie privée originale du fichier n'a été retrouvée dans le dépôt ou les emplacements locaux parcourus. Une sauvegarde chiffrée externe reste à localiser ou à confirmer.

## Validation restante

1. Pousser la candidate Movynex 1.0.3 sur `main`, puis valider les tests unitaires et l'APK debug par GitHub Actions.
2. Vérifier sur le téléphone les écrans Recherche, OAuth/Drive, lecteur, distribution, historique, téléchargements et réglages.
3. Vérifier l'APK release 1.0.3 signée puis la copier dans `H:\Downloads`.
4. Ne reprendre le chantier bilingue de `LANGUAGE_AUDIT.md` que si le besoin familial évolue.

## Dette et améliorations futures

- Retirer les journaux `Log.d` qui peuvent contenir le client OAuth, un code d'autorisation ou des jetons avant toute distribution plus large.
- Moderniser progressivement le design de l'interface.
- Conserver la migration SAF uniquement comme expérience séparée tant que la lecture distante n'est pas immédiate.
