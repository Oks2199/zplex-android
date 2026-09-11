# Movynex — état d’avancement

Dernière mise à jour : 11 septembre 2026

## État actuel

- L’APK `Movynex-1.0.0.apk` est installé et fonctionne sur le téléphone.
- La connexion à Google Drive fonctionne.
- Les films présents sur Google Drive sont indexés et se lancent correctement.
- Le plantage provoqué par la locale française lors de l’ouverture d’un film est corrigé.
- La future mise à jour est préparée avec `versionCode = 2` et `versionName = 1.0.1`.
- Aucun APK 1.0.1 final ne doit être construit tant que le nettoyage visuel n’est pas terminé et validé par l’utilisateur.

## Identité Movynex terminée

- Nom public Android : **Movynex**.
- Package Android : `com.cursedcrew.movynex`.
- Signature release : clé **Cursed Crew**.
- Nouveau logo Movynex créé en bleu et violet.
- Icônes Android release et debug remplacées pour toutes les densités.
- Identifiants internes du lecteur et dossier public de téléchargements renommés pour Movynex.
- Nom du projet Gradle remplacé par Movynex.

## Google Cloud et OAuth

- Projet Google Cloud renommé **Movynex Personal**.
- Nom affiché sur l’écran de consentement OAuth remplacé par **Movynex**.
- Logo Movynex ajouté et enregistré sur l’écran de consentement.
- Client OAuth renommé **Movynex Android**.
- Le Client ID, le Client Secret et les utilisateurs de test existants ont été conservés.
- Le scope Google Cloud et le code Android ont été réduits à la lecture seule : `https://www.googleapis.com/auth/drive.readonly`.
- Le passage du statut OAuth de **Test** à **En production** reste à effectuer pour supprimer l’expiration des autorisations après sept jours.
- Le logo OAuth doit être conservé selon le choix de l’utilisateur. Sans domaine vérifiable, la publication OAuth en production reste bloquée.
- Une solution sans domaine a été identifiée : remplacer OAuth/Drive REST par le sélecteur de dossiers Android (`ACTION_OPEN_DOCUMENT_TREE`). Chaque utilisateur sélectionnerait une fois son dossier Google Drive, puis Android conserverait l’accès en lecture aux fichiers et sous-dossiers. Cette migration reste à valider et à implémenter.
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
- La construction GitHub déclenchée après le commit `c58bcd5` a été annulée à la demande de l’utilisateur avant la livraison de la 1.0.1.
- La compilation locale n’a pas pu être utilisée comme validation finale à cause des restrictions de connexion locale de Gradle dans l’environnement Codex.
- La prochaine validation complète devra donc être effectuée par GitHub Actions après accord explicite de l’utilisateur.

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
