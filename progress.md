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

## Google Drive

- Projet Google Cloud renommé **Movynex Personal**.
- Nom affiché sur l’écran de consentement OAuth remplacé par **Movynex**.
- Logo Movynex ajouté et enregistré sur l’écran de consentement.
- Client OAuth renommé **Movynex Android**.
- Le Client ID, le Client Secret et les utilisateurs de test existants ont été conservés.
- Le scope Google Cloud et le code Android ont été réduits à la lecture seule : `https://www.googleapis.com/auth/drive.readonly`.
- La publication OAuth en production reste bloquée sans domaine vérifiable ; ce parcours ne sera donc plus utilisé par l’application.
- La migration vers le sélecteur de dossiers Android (`ACTION_OPEN_DOCUMENT_TREE`) est implémentée sur la branche locale `codex/saf-migration`.
- L’indexation, la lecture MPV et les téléchargements utilisent désormais les fichiers fournis en lecture seule par Android.
- Android conservera l’accès aux dossiers après leur sélection ; il n’y aura plus d’autorisation OAuth expirant après sept jours.
- Les anciens réglages OAuth sont laissés intacts pendant la phase de test pour permettre un retour à la sauvegarde antérieure.
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
- Un point de sauvegarde antérieur à la migration existe dans le commit local `ceea2ac`.
- La migration sans OAuth est en attente de compilation distante et de test sur le téléphone.

## Construction et vérification

- Le workflow GitHub utilise Java 17, exécute les tests debug et génère une release ARM64 signée.
- Les secrets GitHub nécessaires à TMDB, OMDb et à la signature Cursed Crew sont configurés.
- La construction GitHub déclenchée après le commit `c58bcd5` a été annulée à la demande de l’utilisateur avant la livraison de la 1.0.1.
- La compilation locale n’a pas pu être utilisée comme validation finale à cause des restrictions de connexion locale de Gradle dans l’environnement Codex.
- La prochaine validation complète devra donc être effectuée par GitHub Actions après accord explicite de l’utilisateur.

## À faire avant la version 1.0.1

1. Regrouper et relire la migration vers le sélecteur de dossiers Android.
2. Demander l’accord explicite de l’utilisateur avant tout push déclenchant la construction de test.
3. Laisser GitHub Actions exécuter les tests et générer l’APK release ARM64 signé.
4. Installer l’APK comme mise à jour, puis sélectionner une fois les dossiers Films et Séries dans Google Drive.
5. Lancer une indexation et tester la lecture, la navigation dans les saisons et un téléchargement hors ligne.
6. Après validation, révoquer l’accès de l’ancienne application OAuth depuis le compte Google et supprimer les anciens identifiants locaux dans une version suivante.
7. Terminer l’audit visuel avant de déclarer la version 1.0.1 finale.

## Points à préserver

- Ne pas modifier `com.cursedcrew.movynex`.
- Ne pas changer la clé de signature Cursed Crew.
- Ne pas exposer les mots de passe, secrets OAuth ou clés API.
- Après la migration, ne pas forcer l’utilisateur à resélectionner ses dossiers Google Drive lors des mises à jour suivantes.
- Ne pas renommer les stockages persistants sans prévoir une migration compatible.
- Conserver la licence et les crédits nécessaires du projet open source d’origine.
