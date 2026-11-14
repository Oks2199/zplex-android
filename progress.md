# Movynex — état d'avancement

Dernière mise à jour : 12 novembre 2026

## Livraison 1.0.7 avec profil et badge « Vu »

- Le 12 novembre 2026, l'utilisateur a confirmé le bon fonctionnement de cette version sur son téléphone Android et son Fire TV Stick sous Fire OS 8.
- Téléphone : `H:\Downloads\Movynex-1.0.7.apk` (39 469 685 octets, ARM64, API 31, code 12). SHA-256 `5893D127B238CAE23C871E9723EE24842D271203E889D48D5BBB16F52EE6994B`.
- Fire TV : `H:\Downloads\Movynex-1.0.7-FireTV-ARMv7.apk` (36 428 373 octets, ARM 32 bits, API 30, code 13). SHA-256 `34A917F6582AD62B15535A06429648ECB2EF0DFCFE52911386A49BA14A619B16`.
- Les deux releases affichent Movynex 1.0.7. Les 33 tests unitaires passent en debug et sur les deux releases. Le lint Fire TV relève 0 erreur. Les contrôles portent sur les packages, les architectures, les versions, MPV, les paramètres OAuth, les clés API, l'alignement ZIP et les signatures v2 des versions remplacées. Les empreintes des copies dans `H:\Downloads` correspondent à celles des APK vérifiées.
- Le [workflow de signature Cursed Crew](https://github.com/Oks2199/zplex-android/actions/runs/43295340477) signe le bloc v2 de l'APK téléphone préparée localement. La branche de signature a été supprimée après vérification. Les APK précédentes sont archivées dans `dist/movynex-1.0.7/` et les nouvelles dans `dist/movynex-1.0.7-code12-13/`.
- Sur émulateur, la mise à jour téléphone du code 11 au code 12 a conservé la date de première installation et un titre ajouté à Ma liste.
- Sur le Stick, `adb install --no-streaming -r` a remplacé la version existante. Le package `com.cursedcrew.movynex.firetv.debug` affiche la version 1.0.7, le code 13 et une API minimale 30. La date de première installation a été conservée. La copie déposée dans `Download/` a la même empreinte SHA-256 que l'APK livrée.

## Repère de lecture terminée

- Le badge « Vu » et l'assombrissement du visuel s'affichent sur les films terminés dans la Bibliothèque, Ma liste et les rangées correspondantes de l'accueil. Chaque épisode terminé porte son propre badge dans la saison.
- Les 33 tests unitaires passent. La variante Fire TV ARM 32 bits compile et le lint relève 0 erreur. Sur l'émulateur TV, le focus reste lisible sur une affiche marquée et un épisode téléchargé affiche le badge avec son état de disponibilité. La fin d'un clip local a été enregistrée à 100 % et le clip est sorti de « Continuer la lecture ».

## Synchronisation du profil

- Le téléphone initialise le profil partagé. La TV récupère ce profil avant de contribuer aux changements de Ma liste et aux progressions des films et épisodes.
- Le scope `drive.appdata` est enregistré dans Google Auth Platform pour **Movynex Personal**. Chaque installation déjà connectée peut demander cette autorisation depuis **Réglages → Synchronisation du profil** avec le compte existant.
- Les 31 tests unitaires et le test Android avec deux bases Room et un Drive simulé passent. Ce test couvre l'attente du téléphone, l'import initial, la mise à jour dans les deux sens, les suppressions et la reprise après une erreur d'envoi. Une ancienne base de démonstration a migré sur l'émulateur avec son catalogue et sa progression visibles.
- La compilation locale Fire TV ARM 32 bits et ses 31 tests unitaires réussissent. Le lint Fire TV relève 0 erreur.
- Le retour utilisateur confirme le fonctionnement de la version livrée sur les deux appareils. Les scénarios détaillés d'import, de suppression et de reprise après erreur ont été vérifiés avec le Drive simulé.

## Interface télévision

- Movynex sélectionne des mises en page TV à partir du mode télévision d'Android. Accueil, Bibliothèque et Ma liste partagent une barre supérieure horizontale avec le logo, la recherche et les réglages. L'accueil affiche le visuel TMDB en paysage, le titre et le bouton « Voir la fiche ». Sur téléphone, la bannière est verticale et la navigation se trouve en bas.
- Les fiches, la recherche, les épisodes, les réglages et le lecteur disposent d'une présentation adaptée au format paysage.
- Le cadre de sélection suit les flèches de la télécommande. Les listes principales peuvent retrouver la carte ouverte au retour. Le lecteur accepte les touches de lecture, de déplacement et Menu.
- La pastille dans la barre indique la rubrique ouverte. Le contour des boutons indique la cible de la télécommande, suit leur fond visible et adapte sa couleur au contraste. Les grilles TV utilisent les colonnes de `GridLayoutManager`, des gouttières de 16 dp et une zone de deux lignes pour aligner les titres et les notes.
- Le nouvel accueil a été vérifié localement sur émulateur Android TV, en 1080p et en 720p avec le texte à 125 %. Le parcours à la télécommande couvre la barre, la bannière, les rangées, le retour de fiche, les trois destinations et la recherche. Les captures de démonstration sont dans `dist/tv-preview/captures/accueil-tv/`.
- Les écrans secondaires utilisent les mêmes composants TV. La distribution utilise des portraits 2:3, les fiches d'acteurs présentent la filmographie en rangée et la biographie dans une fenêtre dédiée, les collections et les saisons utilisent des cartes en grille. Le détail d'épisode présente le synopsis et la distribution. Le choix de saison est centré et le sélecteur Drive réserve une zone distincte pour la validation du dossier.
- Les tests unitaires et la compilation de l'APK debug ARM64 réussissent dans [GitHub Actions](https://github.com/Oks2199/zplex-android/actions/runs/43196796234) sur la PR brouillon [n°4](https://github.com/Oks2199/zplex-android/pull/4). Les captures 1080p et la vérification de la grille à quatre colonnes en 720p sont dans `dist/tv-preview/captures/finitions/`. Elles utilisent des données de démonstration sur un émulateur Android TV.
- Les tests unitaires et l'APK debug des écrans secondaires réussissent dans [GitHub Actions](https://github.com/Oks2199/zplex-android/actions/runs/43202860614) sur la même PR. Les captures des écrans secondaires sont dans `dist/tv-preview/captures/ecrans-secondaires/`.
- Les essais sur émulateur ont vérifié le retour sur le film choisi dans une collection, le portrait agrandi et le retour au profil, le choix de saison et le retour à l'épisode, ainsi que la navigation de dossier en dossier jusqu'au bouton de validation avec des dossiers fictifs.
- Vérifications à la télécommande simulée sur l'émulateur : ouverture d'une fiche depuis la bannière, restauration de la sélection avec Retour, passage des affiches de la Bibliothèque aux onglets et à la loupe, accès aux résultats de recherche et retour au champ, ouverture d'un épisode informatif et lecture d'un fichier local avec pause et avance rapide.
- La passe suivante couvre Découvrir, À venir, les filtres, les confirmations, l'attente de synchronisation, les options du lecteur, la liste de lecture et la récupération après erreur. Les commandes à icône de la barre supérieure utilisent un contour circulaire. Le choix de genre et de tri se parcourt à la télécommande. La date des sorties à venir est triée dans le format ISO transmis par TMDB avant l'affichage en français.
- Sur émulateur, le choix d'un genre, du tri et d'un mot-clé fonctionne avec les flèches. Les trois actions des filtres restent visibles en 720p avec la taille de police à 125 %. Le retour de fiche restaure le titre sélectionné dans À venir. Le test de récupération simule une erreur et revient à l'accueil avec la télécommande. Les captures sont dans `dist/tv-preview/captures/verification-tv/`.
- Les tests unitaires et la compilation debug ARM64 de cette passe réussissent dans [GitHub Actions](https://github.com/Oks2199/zplex-android/actions/runs/43211285617) sur la PR brouillon n°4. Les tests sur l'émulateur ont aussi vérifié la sélection de la vitesse de lecture et l'annulation d'un retrait de l'historique.
- Le Fire TV Stick est couvert par le retour utilisateur. Les vérifications ciblées encore ouvertes sont le parcours complet de la liste d'épisodes dans le lecteur, le contrôle visuel systématique sur téléphone et le parcours TalkBack complet.

## Validation initiale de la connexion Google Drive 1.0.7

- Téléphone et Fire TV utilisent le même bouton **Connecter Google Drive**. Le navigateur remet la réponse à Movynex sur `127.0.0.1:53682`. Movynex vérifie l'état de la tentative et échange le code avec PKCE.
- Une APK privée peut intégrer les deux paramètres du client OAuth renseignés dans `local.properties`. Le fichier est ignoré par Git. Une APK de validation propose les deux champs dans l'application. Les sessions existantes restent dans le même espace DataStore.
- Les tests unitaires et la compilation debug ARM64 passent sur la [pull request brouillon n°4](https://github.com/Oks2199/zplex-android/actions/runs/43228827684). Sur l'émulateur TV, un navigateur de démonstration a envoyé une réponse invalide suivie d'un refus avec l'état valide. Movynex a rejeté la première réponse, affiché le refus au retour et replacé la sélection sur le bouton de connexion.
- Le 12 novembre 2026, les deux variantes debug privées 1.0.7 (code 11) ont été assemblées localement avec les tâches Gradle habituelles. Les 31 tests unitaires passent pour chacune. Le lint Fire TV rapporte 0 erreur et 135 avertissements après remplacement des appels à des API internes de focus. Les deux APK ont été contrôlées pour le package, le libellé Movynex, la version, l'API minimale, l'ABI, MPV, la présence de la configuration locale, l'alignement ZIP et la signature debug.
- Fire TV, API 30 et `armeabi-v7a`, livrée dans `H:\Downloads\Movynex-1.0.7-FireTV-ARMv7-Test.apk` (58 364 133 octets). Son certificat debug correspond à celui de la précédente APK Fire TV. SHA-256 de l'artefact et de la copie `7171C122DD3CE9B30554C9122E8BCFEA15407B8EB6A8B3C6CB1EF71A1947CEFF`.
- Téléphone, API 31 et `arm64-v8a`, livrée dans `H:\Downloads\Movynex-1.0.7-Telephone-ARM64-Test.apk` (61 380 473 octets). Le package debug permet l'installation à côté de la release. SHA-256 de l'artefact et de la copie `8FAC047ACC337DE8837007B9DC795102D496C1EFD1C1E4C2AEC8E42B6360B221`.
- L'écran de l'APK privée ARM64 a été vérifié sur l'émulateur Android TV. Il affiche directement **Connecter Google Drive** avec les champs de configuration masqués.
- L'utilisateur a confirmé la connexion à son compte Google depuis l'APK debug sur son téléphone, puis a supprimé cette copie séparée.

## Premières mises à jour 1.0.7

Les fichiers de cette première livraison sont archivés dans `dist/movynex-1.0.7/`. Les APK des codes 12 (téléphone) et 13 (Fire TV) les ont remplacés dans `H:\Downloads`.

- Les deux APK de mise à jour sont des releases privées non débogables. Les 31 tests unitaires passent pour le téléphone et le Fire TV. Le lint Fire TV relève 0 erreur et 135 avertissements.
- Téléphone : `H:\Downloads\Movynex-1.0.7.apk` (39 391 998 octets, ARM64, API 31, code 11). L'APK conserve le package `com.cursedcrew.movynex` et le certificat Cursed Crew de la 1.0.6. L'émulateur Android a accepté le remplacement avec `adb install -r`, conservé la date de première installation et affiché le bouton Google Drive préconfiguré. SHA-256 de la copie et de l'artefact `58086C1EF5C5E7BA0108F77D947B5F28C08940C091E345533361975392A9C37A`.
- Fire TV : `H:\Downloads\Movynex-1.0.7-FireTV-ARMv7.apk` (36 350 666 octets, ARM 32 bits, API 30, code 12). La version affichée reste 1.0.7. Le package et le certificat correspondent aux APK Fire TV précédentes. SHA-256 de la copie et de l'artefact `1F35BE023AC64066380FA0AA1FBB3F682FC76B05B976B6A58DE1C694B814E876`.
- Les valeurs OAuth et les APK ont été gardées sur le PC. Pour le téléphone, un [workflow ponctuel](https://github.com/Oks2199/zplex-android/actions/runs/43241671649) a signé les données du bloc v2, qui contiennent l'empreinte de l'APK et le certificat public. La branche ponctuelle a été retirée après la vérification. À cette étape, `main` contenait encore la 1.0.6.

## APK de test Fire TV

- Variante locale `1.0.6-FIRETV-DEBUG` (code 10) pour Fire OS 8, Android 11/API 30 et ARM 32 bits (`armeabi-v7a`). Le package `com.cursedcrew.movynex.firetv.debug` utilise la signature debug et une session Google Drive séparée.
- Le lanceur TV est déclaré dans le manifeste de la variante. Le lecteur réserve les options avancées du mode image dans l'image à Android 12 et masque le bouton si l'appareil ne prend pas ce mode en charge.
- Les 27 tests unitaires passent pour Fire TV et pour le debug standard. Le formatage d'une taille en octets est désormais indépendant de la langue système et le test le vérifie avec une locale française.
- Les deux assemblages debug réussissent. Le lint Fire TV relève 0 erreur et 122 avertissements. L'APK Fire TV a été contrôlée avec AAPT, `zipalign` et `apksigner` (signature debug v2). Elle contient les bibliothèques MPV 32 bits et les deux clés API attendues.
- APK de test copiée dans `H:\Downloads\Movynex-1.0.6-FireTV-ARMv7-Test.apk` (58 162 675 octets). La copie et l'artefact source ont le SHA-256 `E67DB0CC078318A8C2E7D19AD091907E99EE37BFBD8039C2B439F7BE118D0E3F`.
- Le 11 novembre 2026, l'utilisateur a confirmé que l'APK fonctionne sur son Stick. Il a signalé que la présentation reste pensée pour le téléphone et que la saisie de la connexion Google Drive à la télécommande est difficile. Le retour ne précise pas les fonctions de lecture essayées. La procédure de compilation est documentée dans `PROJECT_CONTEXT.md`.

## Versions précédentes

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

## Référence historique de la 1.0.6

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
- La 1.0.6 a été intégrée à `main` au commit de fusion `fdf4e01`.
- La [pull request n°4](https://github.com/Oks2199/zplex-android/pull/4), issue de `codex/fire-tv-interface`, suit l'intégration du code de la 1.0.7 livrée.
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

- Le fonctionnement général de Movynex 1.0.7 est confirmé par l'utilisateur sur ses deux appareils. Les contrôles ciblés encore ouverts figurent dans la section « Interface télévision ».
- Ne reprendre le chantier bilingue de `LANGUAGE_AUDIT.md` que si le besoin familial évolue.

## Dette et améliorations futures

- Retirer les journaux `Log.d` qui peuvent contenir le client OAuth, un code d'autorisation ou des jetons avant toute distribution plus large.
- Moderniser progressivement le design de l'interface.
- Conserver la migration SAF uniquement comme expérience séparée tant que la lecture distante n'est pas immédiate.
