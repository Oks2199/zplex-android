# Movynex — état d’avancement

Dernière mise à jour : 11 septembre 2026

## État actuel

- La version SAF expérimentale (`versionCode = 4`) est encore installée sur le téléphone au moment de cette mise à jour.
- La connexion à Google Drive fonctionne.
- Les films présents sur Google Drive sont indexés correctement.
- Le plantage provoqué par la locale française lors de l’ouverture d’un film est corrigé.
- La restauration du lecteur Google Drive OAuth est prête avec `versionCode = 5` et `versionName = 1.0.1`.
- L’APK signée à valider est disponible dans `H:\Downloads\Movynex-1.0.1.apk`.
- Aucun APK 1.0.1 final ne doit être construit tant que le nettoyage visuel n’est pas terminé et validé par l’utilisateur.

## Identité Movynex terminée

- Nom public Android : **Movynex**.
- Package Android : `com.cursedcrew.movynex`.
- Signature release : clé **Cursed Crew**.
- Nouveau logo Movynex créé en bleu et violet.
- L'icône Android utilise désormais le grand **M** du logo sur fond presque noir, dans un style d'icône simple et lisible comparable à Netflix. Le mot-symbole complet reste réservé à l'intérieur de l'application.
- Toutes les densités release, debug et monochromes ont été régénérées depuis le logo haute définition ; les anciennes images ZPlex des variantes debug ont été supprimées.
- Identifiants internes du lecteur et dossier public de téléchargements renommés pour Movynex.
- Nom du projet Gradle remplacé par Movynex.

## Google Cloud et OAuth

- Projet Google Cloud renommé **Movynex Personal**.
- Nom affiché sur l’écran de consentement OAuth remplacé par **Movynex**.
- Logo Movynex ajouté et enregistré sur l’écran de consentement.
- Client OAuth renommé **Movynex Android**.
- Le Client ID, le Client Secret et les utilisateurs de test existants ont été conservés.
- Le scope Google Cloud et le code Android ont été réduits à la lecture seule : `https://www.googleapis.com/auth/drive.readonly`.
- Le statut OAuth est passé de **Testing** à **In production** le 11 septembre 2026 : les autorisations Google Drive ne sont plus limitées à sept jours.
- L'application reste volontairement non validée pour cet usage familial privé. Google peut afficher un avertissement lors de la première connexion et applique une limite de 100 utilisateurs OAuth sur la durée de vie du projet.
- Site public : `https://sites.google.com/view/movynex/home`.
- Politique de confidentialité : `https://sites.google.com/view/movynex/privacy-policy`.
- Conditions d'utilisation : `https://sites.google.com/view/movynex/terms-of-service`.
- Le domaine OAuth autorisé pour ces pages Google Sites est `google.com`.
- Une migration vers le sélecteur de dossiers Android (`ACTION_OPEN_DOCUMENT_TREE`) a été testée sur la branche `codex/saf-migration`. Elle est abandonnée pour la lecture vidéo : le fournisseur Google Drive ne permet pas à MPV d’atteindre immédiatement les zones éloignées d’un MKV et peut provoquer plusieurs minutes d’écran noir pendant le chargement.
- URI de redirection utilisée : `http://127.0.0.1:53682/`.

## Corrections déjà enregistrées dans Git

- `cf609ce` — correction du plantage lié au format décimal français.
- `2013477` — création de l’identité release Movynex, package, signature et workflow GitHub.
- `c58bcd5` — remplacement des icônes et nettoyage principal de l’identité Movynex.
- Ces changements sont présents sur `origin/main`.

## Modifications locales en cours

- Le texte Movynex situé en haut à gauche de l’accueil est remplacé par le logo horizontal.
- Le logo horizontal est stocké dans `app/src/main/res/drawable-nodpi/movynex_wordmark.png`.
- Le logo est affiché uniquement sur l’accueil ; les autres écrans conservent leurs titres fonctionnels.
- `AGENTS.md` a été ajouté pour protéger l’identité, la signature, les secrets et la procédure de livraison.
- `progress.md` sert de journal d’avancement et doit rester à jour après chaque étape importante.
- Le dossier `dist/` reste volontairement hors de Git.

## Construction et vérification

- Le workflow GitHub utilise Java 17, exécute les tests debug et génère une release ARM64 signée.
- Les secrets GitHub nécessaires à TMDB, OMDb et à la signature Cursed Crew sont configurés.
- La restauration OAuth est isolée sur `codex/oauth-restoration` au commit `f50c471`; l’expérience SAF reste conservée sur `codex/saf-migration` au commit `cd27201`.
- GitHub Actions #9 a terminé avec succès : tests, APK debug et APK release signée.
- L’APK release a été vérifiée : package `com.cursedcrew.movynex`, versionCode `5`, versionName `1.0.1`, signature Cursed Crew `88bc4c54789d5bc00a921425ea7a92e2d66aa72c29d37d25c63e9cb242b76832`.
- Empreinte SHA-256 de l’APK : `BC7676D3DF3BE32B63E8526904EA108E66F6C0855263E81EF52906203B9B1390`.

## À faire avant la version 1.0.1

1. Terminer l’audit visuel de l’application et relever toute autre référence visible à l’ancien nom.
2. Vérifier la taille et l’alignement du logo dans la barre supérieure de l’accueil.
3. Regrouper et relire les modifications locales.
4. Demander l’accord explicite de l’utilisateur avant tout push déclenchant la construction finale.
5. Laisser GitHub Actions exécuter les tests et générer l’APK release ARM64 signé.
6. Vérifier le package, la version, le nom, l’architecture, les clés API et la signature de l’APK.
7. Copier l’APK final vérifié dans `H:\Downloads` pour installation comme mise à jour.

## Points à préserver

- Ne pas modifier `com.cursedcrew.movynex`.
- Ne pas changer la clé de signature Cursed Crew.
- Ne pas exposer les mots de passe, secrets OAuth ou clés API.
- Ne pas forcer l’utilisateur à reconfigurer Google Drive lors d’une mise à jour.
- Ne pas renommer les stockages persistants sans prévoir une migration compatible.
- Conserver la licence et les crédits nécessaires du projet open source d’origine.
