# Movynex — consignes du projet

Ce fichier contient les règles persistantes à respecter lors de toute modification du dépôt.

## Reprise du projet

- Lire `PROJECT_CONTEXT.md` et `progress.md` en entier avant de commencer une nouvelle session de travail.
- Traiter `PROJECT_CONTEXT.md` comme la source de vérité pour les chemins, la compilation, la signature, Google Cloud, Google Sites et l'historique technique.
- Maintenir le dossier de reprise après toute modification de la procédure de build, de la signature, d'OAuth, des services externes ou des emplacements de livraison.
- Ne jamais ajouter de valeur secrète au dossier de reprise, même pour faciliter une session future.

## Identité du produit

- Le nom public de l'application est **Movynex**.
- L'identifiant Android officiel est `com.cursedcrew.movynex`. Ne jamais le changer pour une mise à jour existante.
- Le namespace Kotlin historique `zechs.zplex` reste interne tant qu'une migration complète, planifiée et testée n'est pas explicitement demandée.
- Les noms internes historiques peuvent rester dans les packages, identifiants de vues et fichiers de navigation. Ils ne doivent jamais être affichés à l'utilisateur.
- Aucune mention visible de l'ancien nom ZPlex ne doit apparaître dans l'application, son icône, ses notifications, ses dossiers de téléchargement ou le parcours OAuth.
- Conserver la licence MIT et les crédits nécessaires du projet open source d'origine.

## Identité graphique

- L'icône Android utilise le grand monogramme **M** bleu/violet sur fond `#15171D`.
- Les couches release sont `app/src/main/res/mipmap-*/ic_launcher_foreground.png`.
- Les variantes debug et monochromes doivent rester cohérentes avec la release et ne contenir aucun ancien visuel.
- Le mot-symbole horizontal est `app/src/main/res/drawable-nodpi/movynex_wordmark.png`.
- Sur l'accueil, afficher le mot-symbole dans la barre supérieure. Sur les autres écrans, conserver les titres fonctionnels.
- Toute ressource de marque doit rester lisible sur fond sombre et dans les masques Android ronds, carrés ou adaptatifs.

## Version et compatibilité

- Version livrée de référence : `versionCode = 5`, `versionName = 1.0.1`.
- Android minimum : API 31 ; compilation et cible : API 36.
- Incrémenter `versionCode` pour chaque APK destinée à remplacer une version déjà installée.
- Utiliser un `versionName` au format `MAJEUR.MINEUR.CORRECTIF`.
- Préserver les réglages locaux, la connexion Google Drive, l'indexation, l'historique et les téléchargements lors des mises à jour.
- Ne pas renommer une base de données, une clé DataStore ou un stockage persistant sans migration compatible.

## Google Drive et OAuth

- Projet Google Cloud : **Movynex Personal**.
- Nom public OAuth : **Movynex**.
- Client OAuth : **Movynex Android**, de type application de bureau.
- Scope attendu dans Google Cloud et dans Android : `https://www.googleapis.com/auth/drive.readonly`.
- URI de redirection attendue : `http://127.0.0.1:53682/`.
- Audience : externe ; statut : **In production**.
- Domaine autorisé pour les pages Google Sites : `google.com`.
- Pages publiques :
  - `https://sites.google.com/view/movynex/home`
  - `https://sites.google.com/view/movynex/privacy-policy`
  - `https://sites.google.com/view/movynex/terms-of-service`
- Ne jamais recréer ou remplacer le Client ID, le Client Secret, les scopes ou l'audience sans demande explicite.
- Un changement de scope impose normalement une nouvelle autorisation Google aux utilisateurs.
- Ne jamais écrire les identifiants OAuth, codes d'autorisation, jetons ou clés d'API dans Git ou dans une réponse utilisateur.
- Le trafic HTTP en clair autorisé dans le manifeste sert au retour OAuth local sur `127.0.0.1`; ne pas le retirer sans remplacer ce mécanisme.

## Lecture Google Drive

- Conserver le lecteur OAuth direct actuellement fonctionnel.
- La tentative basée sur le sélecteur Android SAF est conservée sur `codex/saf-migration`, mais elle n'est pas destinée à la release : le fournisseur Google Drive peut charger un MKV en entier avant de permettre les recherches nécessaires à MPV, ce qui provoque un long écran noir.
- Ne pas réintroduire la migration SAF sans test concluant de lecture immédiate et de déplacement dans une vidéo distante.
- Toute conversion de nombres doit être indépendante de la langue du téléphone, notamment avec la locale française.

## Clés API, jetons et signature

- TMDB et OMDb sont injectés depuis `local.properties` en local et depuis les secrets GitHub en CI.
- La release doit rester signée avec la clé **Cursed Crew** pour pouvoir mettre à jour l'application installée.
- Variables de signature attendues : `MOVYNEX_KEYSTORE_PATH`, `MOVYNEX_KEYSTORE_PASSWORD`, `MOVYNEX_KEY_ALIAS` et `MOVYNEX_KEY_PASSWORD`.
- Ne jamais afficher, copier dans les journaux, committer ou remplacer une clé, un mot de passe ou un secret.
- Dette de sécurité connue : certains appels `Log.d` historiques exposent le client OAuth ou les jetons dans les journaux de diagnostic. Les supprimer avant toute distribution plus large que le cercle familial.

## Construction et livraison

- Utiliser JDK 17. Le workflow `.github/workflows/android.yml` teste le debug puis produit une release ARM64 signée.
- Un push sur `main` déclenche automatiquement une construction GitHub.
- Un lancement manuel GitHub Actions ne voit que les commits déjà présents sur la branche distante sélectionnée ; il ne peut jamais tester des modifications locales non poussées.
- Dans l'environnement Codex Windows, l'erreur Gradle `Unable to establish loopback connection` peut survenir avant la configuration du projet. Après confirmation par une tentative avec `--stacktrace`, la traiter comme une limite de l'environnement, ne pas modifier Gradle pour la contourner et utiliser GitHub Actions pour la validation complète.
- Si Gradle est bloqué localement, une pull request vers `main` permet de valider les tests et l'APK debug sans produire de release signée. La livraison se fait ensuite seulement après validation et accord explicite pour intégrer ou pousser sur `main`.
- Ne pas pousser une modification applicative sur `main` sans accord explicite de l'utilisateur pour lancer la construction.
- Pour une modification exclusivement documentaire, un commit contenant `[skip ci]` peut être utilisé afin d'éviter une construction inutile.
- Avant une livraison, exécuter les tests unitaires et une compilation debug, puis vérifier la release signée produite par GitHub Actions.
- Vérifier au minimum le package, `versionCode`, `versionName`, le libellé Movynex, l'architecture ARM64, les clés API et la signature Cursed Crew.
- Copier uniquement l'APK finale vérifiée dans `H:\Downloads` lorsque l'utilisateur demande une livraison.
- Garder `dist/` hors de Git.

## Documentation et suivi

- `README.md` décrit l'installation, l'utilisation, le nommage des médias, OAuth et le développement.
- `PROJECT_CONTEXT.md` contient toutes les informations nécessaires à la reprise du projet dans une nouvelle conversation.
- `progress.md` décrit uniquement l'état réel, les livraisons vérifiées, les validations restantes et la dette connue.
- Mettre ces deux fichiers à jour après une étape importante, sans y inscrire de secret.
- Ne pas annoncer une modification comme publiée, testée ou installée sans preuve correspondante.

## Qualité des modifications

- Préserver les changements existants de l'utilisateur et éviter les refactorisations sans rapport avec la demande.
- Vérifier les écrans concernés avant de déclencher une release après une modification visuelle.
- Conserver `origin` sur le fork familial et `upstream` sur le dépôt ZPlex d'origine.
- Ne pas fusionner `codex/saf-migration` dans `main` sans demande et validation explicites.
