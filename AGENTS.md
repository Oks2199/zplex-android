# Movynex — consignes du projet

## Identité du produit

- Le nom public de l’application est **Movynex**.
- L’identifiant Android officiel est `com.cursedcrew.movynex`. Ne jamais le changer pour une mise à jour existante.
- Le `namespace` Kotlin historique reste `zechs.zplex` tant qu’une migration complète, planifiée et testée n’est pas explicitement demandée. Il est interne et ne doit jamais être affiché à l’utilisateur.
- Aucune mention visible de l’ancien nom ZPlex ne doit apparaître dans l’application, son icône, ses notifications, ses dossiers publics ou le parcours OAuth.
- Conserver les mentions d’origine nécessaires dans la licence et les crédits du projet open source.

## Identité graphique

- L’icône Android release utilise les fichiers `app/src/main/res/mipmap-*/ic_launcher_foreground.png`.
- Le logo horizontal de l’accueil est `app/src/main/res/drawable-nodpi/movynex_wordmark.png`.
- Sur l’accueil, afficher le logo dans la barre supérieure à la place du titre texte. Sur les autres écrans, conserver les titres fonctionnels habituels.
- Toute nouvelle ressource de marque doit rester lisible sur fond sombre et dans les masques d’icône Android ronds ou carrés.

## Version et compatibilité

- Incrémenter `versionCode` à chaque APK destinée à remplacer une version déjà installée.
- Utiliser un `versionName` clair suivant le format `MAJEUR.MINEUR.CORRECTIF`.
- Préserver les réglages locaux, la connexion Google Drive, l’indexation, l’historique et les téléchargements lors des mises à jour.
- Ne pas renommer une base de données, une clé de préférences ou un stockage persistant sans migration compatible.

## Google Drive et OAuth

- Projet Google Cloud : **Movynex Personal**.
- Nom public OAuth : **Movynex**.
- Client OAuth : **Movynex Android**.
- Scope Drive attendu : `https://www.googleapis.com/auth/drive`.
- URI de redirection attendue : `http://127.0.0.1:53682/`.
- Ne jamais recréer ou remplacer le Client ID, le Client Secret, les scopes ou les utilisateurs de test sans demande explicite.
- Ne jamais écrire les identifiants OAuth ou les clés d’API dans Git.

## Clés API et signature

- TMDB et OMDb sont injectés depuis `local.properties` en local et depuis les secrets GitHub en CI.
- La release doit rester signée avec la clé **Cursed Crew** afin que les APK suivantes puissent mettre à jour l’application installée.
- Les variables de signature attendues sont `MOVYNEX_KEYSTORE_PATH`, `MOVYNEX_KEYSTORE_PASSWORD`, `MOVYNEX_KEY_ALIAS` et `MOVYNEX_KEY_PASSWORD`.
- Ne jamais afficher, copier dans les journaux, committer ou remplacer une clé, un mot de passe ou un secret.

## Construction et livraison

- Le workflow `.github/workflows/android.yml` teste le debug puis construit l’APK release ARM64 signé.
- Un push sur `main` déclenche automatiquement une construction GitHub. Ne pas pousser tant que l’utilisateur n’a pas explicitement confirmé qu’il souhaite construire l’APK final.
- Avant une livraison, exécuter les tests unitaires et une compilation debug avec Java 17, puis vérifier la release signée produite par GitHub Actions.
- Vérifier au minimum le package, la version, le libellé Movynex, l’architecture ARM64 et la signature Cursed Crew.
- Copier uniquement l’APK final vérifié dans `H:\Downloads` quand l’utilisateur demande une livraison.
- Garder `dist/` hors de Git.

## Qualité et sécurité des modifications

- Corriger les conversions numériques sans dépendre de la langue du téléphone ; l’application doit fonctionner avec la locale française.
- Éviter les refactorisations mécaniques massives du namespace historique pendant une modification d’interface.
- Ne pas supprimer les changements existants de l’utilisateur et ne pas modifier les fichiers sans rapport avec la demande.
- Pour toute modification visuelle, vérifier les écrans concernés avant de déclencher une nouvelle release.
