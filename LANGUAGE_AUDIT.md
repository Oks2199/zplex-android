# Movynex — audit de la sélection de langue

Date de l'audit : 12 septembre 2026

## Statut

Cet audit décrit comment ajouter correctement une sélection de langue à Movynex. Il ne signifie pas que la fonctionnalité est déjà implémentée.

**Décision du 12 septembre 2026 : la sélection de langue n'est pas prévue pour le moment.** Movynex reste une application familiale en français. L'audit est conservé comme référence si une version bilingue devient utile plus tard ; son plan et sa checklist ne constituent plus le prochain chantier.

État au moment de l'audit :

- Movynex 1.0.2 est installée et fonctionne sur le téléphone ; la 1.0.3 est construite, vérifiée et prête à être installée par-dessus.
- Movynex 1.0.3 traduit le champ de recherche et réalise un passage global des textes visibles de l'interface, des erreurs, des notifications et des tâches de fond.
- Les requêtes TMDB utilisent actuellement `fr-FR` et la région `FR` de manière fixe.
- Aucun sélecteur de langue, aucune ressource `values-fr` et aucune préférence de langue n'existent encore.
- La correction française est livrée dans Movynex 1.0.3 avec `versionCode = 7` afin de remplacer la 1.0.2.

## Conclusion de l'audit

La fonctionnalité est réalisable avec les dépendances actuelles. Movynex utilise :

- `minSdk = 31`, `compileSdk = 36` et `targetSdk = 36` ;
- Android Gradle Plugin 8.12.2 ;
- AppCompat 1.7.1 ;
- des activités dérivées de `AppCompatActivity`.

La bonne solution repose sur le mécanisme officiel de langue par application d'Android et d'AndroidX. Elle ne doit pas être construite comme une simple préférence interne qui remplace manuellement la `Locale` globale.

Choix recommandé dans les réglages :

1. **Système** — aucune locale imposée par Movynex ;
2. **Français** — locale applicative `fr` et requêtes TMDB `fr-FR` ;
3. **English** — locale applicative `en` et requêtes TMDB `en-US`.

La région TMDB doit rester indépendante de la langue. Pour l'usage actuel, conserver `FR` permet de garder les dates et filtres de sortie français même lorsque l'interface est affichée en anglais.

## Sources officielles consultées

- Android, préférences de langue par application : <https://developer.android.com/guide/topics/resources/app-languages>
- Android, localisation des ressources : <https://developer.android.com/guide/topics/resources/localization>
- AndroidX, `AppCompatDelegate` : <https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate>
- AndroidX, contexte localisé hors activité : <https://developer.android.com/reference/androidx/core/content/ContextCompat>
- TMDB, fonctionnement des langues : <https://developer.themoviedb.org/docs/languages>
- TMDB, fonctionnement des régions : <https://developer.themoviedb.org/docs/region-support>
- TMDB, traductions principales prises en charge : <https://developer.themoviedb.org/reference/configuration-primary-translations>

## Architecture Android recommandée

### Ressources

Créer une vraie séparation des traductions :

- `app/src/main/res/values/strings.xml` : ressources anglaises complètes et langue de secours ;
- `app/src/main/res/values-fr/strings.xml` : traductions françaises complètes ;
- conserver les noms de ressources identiques dans les deux fichiers ;
- marquer comme non traduisibles les valeurs purement techniques, par exemple l'URL du scope Drive et les exemples OAuth appropriés ;
- déplacer dans les ressources tous les textes visibles actuellement écrits directement dans Kotlin.

Le fichier par défaut doit toujours rester complet. Une ressource française manquante retombera alors sur l'anglais au lieu de provoquer une ressource introuvable.

### Déclaration des langues prises en charge

Avec AGP 8.12.2, utiliser la génération automatique recommandée par Android :

- activer `androidResources.generateLocaleConfig = true` dans `app/build.gradle.kts` ;
- ajouter `app/src/main/res/resources.properties` avec la locale par défaut ;
- limiter les configurations de ressources à `en` et `fr` pour éviter que les traductions fournies par les bibliothèques Android fassent apparaître des langues que Movynex ne traduit pas réellement.

Cette configuration fera apparaître Movynex dans le réglage système « Langue de l'application » sur Android 13 et versions suivantes.

### Stockage et application du choix

Utiliser :

- `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr"))` pour le français ;
- `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))` pour l'anglais ;
- `AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())` pour suivre le système ;
- `AppCompatDelegate.getApplicationLocales()` pour afficher la sélection courante.

L'appel doit être effectué sur le thread principal. Android recrée normalement l'activité afin de recharger les ressources dans la nouvelle langue.

Pour Android 12 et 12L, ajouter dans le manifeste le service de métadonnées AppCompat avec `autoStoreLocales=true`. Ce stockage doit rester séparé du `SessionManager` actuel : `SessionManager.resetDataStore()` efface les données de session lors de la déconnexion et ne doit pas réinitialiser la langue choisie.

### Écran Réglages

Ajouter au début de `fragment_settings.xml` une section générale contenant :

- titre : « Général » / « General » ;
- ligne : « Langue » / « Language » ;
- résumé dynamique : « Système », « Français » ou « English » ;
- boîte de dialogue à choix unique utilisant les trois options recommandées.

Le clic applique la locale avec AppCompat. Le résumé sera recalculé après la recréation de l'activité.

## Propagation vers TMDB

### État actuel

`Constants.TMDB_LANGUAGE` vaut actuellement `fr-FR`. Cette constante sert de valeur par défaut à presque toutes les méthodes de `TmdbApi`, ce qui empêche tout changement à l'exécution.

### Modification recommandée

Introduire un fournisseur central, par exemple `AppLanguageProvider`, capable de retourner :

- la locale Android effective ;
- le code TMDB correspondant ;
- la langue stable à inclure dans les clés de cache.

Correspondance recommandée :

| Choix effectif | Ressources Android | Langue TMDB | Région TMDB |
| --- | --- | --- | --- |
| Français | `fr` | `fr-FR` | `FR` |
| English | `en` | `en-US` | `FR` |
| Système en français | `fr` | `fr-FR` | `FR` |
| Autre langue système | anglais de secours | `en-US` | `FR` |

Le dépôt devra passer explicitement le paramètre `language` aux méthodes Retrofit au lieu de dépendre d'une constante figée. Le paramètre `region` doit rester géré séparément.

Limite externe connue : TMDB ne traduit pas nécessairement toutes les données. Les noms de personnes, certains personnages, titres ou synopsis absents de la traduction peuvent rester dans leur langue originale ou être vides.

## Formats de date et de nombre

L'audit a trouvé des formats d'affichage forcés :

- `Media.kt`, `MediaViewModel.kt`, `EpisodesViewModel.kt` et `SeasonViewHolder.kt` utilisent `Locale.FRENCH` pour des dates visibles ;
- `IndexingServiceStateManager.kt` utilise `Locale.ENGLISH` pour l'heure de la dernière indexation ;
- `PersonResponse.kt`, `CastViewHolder.kt` et `Converter.kt` construisent certains textes visibles directement en anglais.

Les dates visibles doivent utiliser la locale applicative effective. Les textes comme « ans », « né en », « décédé en », les durées et les tailles doivent provenir de ressources traduites, avec des pluriels lorsque nécessaire.

En revanche, les formats techniques doivent rester indépendants de la langue :

- parsing des dates ISO `yyyy-MM-dd` ;
- conversion de nombres destinés à être relus par le programme ;
- calcul des notes ;
- formats de noms `SxxExx` ;
- dossiers Drive `Season N`.

La correction historique utilisant `Locale.ENGLISH` pour éviter une `NumberFormatException` ne doit pas être annulée.

## Contextes hors activité

Les fragments et vues utilisent normalement un contexte qui suit la locale AppCompat. Plusieurs ViewModels héritent cependant de `BaseAndroidViewModel` et lisent les ressources depuis l'objet `Application`.

Sur Android 12 et versions antérieures, ces accès doivent utiliser un contexte compatible avec la langue de l'application, par exemple `ContextCompat.getContextForLanguage(applicationContext)`, ou les méthodes AndroidX équivalentes. Cela concerne notamment :

- `HomeViewModel` ;
- `SearchViewModel` ;
- `MediaViewModel` ;
- `EpisodesViewModel` ;
- les Workers et services qui créent des notifications.

Sans cette correction, l'écran pourrait être français tandis qu'un message d'erreur ou une notification resterait anglais.

## Cache API

### Risque actuel

Les clés de `TmdbRepository` ressemblent à :

- `movie_<id>...` ;
- `show_<id>...` ;
- `<id>_season_<numéro>` ;
- `movie_company_<id>_<page>` ;
- `show_company_<id>_<page>`.

Elles ne contiennent pas la langue. Après un changement, une réponse française pourrait donc être retournée pendant trente jours à une interface anglaise, ou inversement.

### Solution

- inclure la langue effective dans chaque clé, par exemple `fr-FR_movie_123...` ;
- appliquer cette règle aux films, séries, saisons et listes par société ;
- ne pas mélanger les caches de deux langues ;
- laisser les anciennes clés expirer ou effectuer un nettoyage unique lors de la mise à jour ;
- conserver le bouton manuel de remise à zéro du cache comme outil de diagnostic.

Le changement de langue ne devrait pas exiger une intervention manuelle de l'utilisateur.

## Métadonnées persistantes

La remise à zéro de `api_cache.db` ne suffit pas. D'autres bases conservent des textes localisés :

### Bibliothèque Drive et watchlist

La base historique `zplex_db.db` conserve :

- le titre des films ;
- le nom des séries ;
- les noms enregistrés dans l'historique de lecture.

L'indexeur ne remplace actuellement pas ces titres pour un média déjà connu si seul le langage change.

### Téléchargements hors ligne

La base hors ligne conserve des réponses TMDB complètes sous forme de JSON pour :

- les films ;
- les séries ;
- les saisons.

Ces données ne doivent pas être supprimées avec les fichiers vidéo.

### Actualisation recommandée

Après un changement de langue :

1. planifier un travail unique avec WorkManager et une contrainte réseau ;
2. recharger les métadonnées des identifiants TMDB déjà enregistrés ;
3. mettre à jour les titres de bibliothèque et d'historique ;
4. remplacer les JSON hors ligne lorsque le réseau est disponible ;
5. préserver les `fileId`, `modifiedTime`, chemins locaux, historiques, progressions et fichiers téléchargés ;
6. réessayer automatiquement si le changement a eu lieu hors connexion.

Un petit stockage applicatif séparé doit mémoriser la dernière langue de métadonnées traitée. Cela permet de détecter aussi un changement effectué depuis les réglages système Android, en dehors de Movynex.

## Notifications et tâches de fond

L'audit a trouvé des textes visibles écrits directement en anglais dans :

- `DownloadWorker.kt` : progression, quantité téléchargée, temps restant, vitesse et échec ;
- `OfflineDatabaseWorker.kt` : téléchargement terminé ;
- `RemoteLibraryIndexingService.kt` : nom du canal d'indexation ;
- `RemoteLibraryRepository.kt` : étapes de l'indexation.

Tous doivent passer par des ressources localisées et un contexte AndroidX respectant la langue de l'application. Les identifiants internes des canaux de notification restent inchangés.

## Logique dépendant actuellement du texte affiché

`BrowseFragment.kt` utilise les chaînes visibles comme valeurs de contrôle :

- comparaisons avec `"Movies"` et `"TV Shows"` ;
- noms de genres comme clés de `Map` ;
- noms de tri comme clés avant conversion vers `SortBy`.

Une traduction directe peut donc casser le choix du type de média, du genre ou du tri. Cet écran est actuellement masqué dans la navigation, mais il doit être corrigé avant de considérer la localisation comme complète.

Refactorisation nécessaire :

- utiliser l'identifiant du bouton sélectionné pour déterminer `MediaType` ;
- associer chaque genre à son ID numérique TMDB et à une ressource de libellé ;
- associer chaque tri directement à l'enum `SortBy` et à une ressource de libellé ;
- ne jamais convertir un texte traduit avec `valueOf()` ;
- ne jamais comparer un libellé visible pour prendre une décision métier.

La comparaison du bouton « Selected » dans `SettingsFragment` est également fragile et devrait utiliser directement l'état du dossier fourni par le ViewModel.

## Textes encore à extraire ou traduire

L'audit global a relevé plusieurs dizaines de textes anglais encore visibles. Les groupes principaux sont :

- bibliothèque, watchlist, ajout et retrait d'un média ;
- connexion OAuth et configuration Google Drive ;
- sélecteur de dossiers et de fichiers ;
- lecteur vidéo, pistes, chapitres, vitesse et Picture-in-Picture ;
- historique ;
- téléchargement et indexation ;
- distribution, âge, genre et biographie ;
- messages d'erreur et écran de plantage ;
- Discover, Upcoming, filtres, genres et tris actuellement masqués.

Les journaux `Log.d` et `Log.e` peuvent rester en anglais tant qu'ils ne sont pas visibles. Les journaux contenant des secrets OAuth constituent une dette de sécurité distincte et doivent être supprimés avant toute distribution élargie.

## Effets du changement à chaud

`AppCompatDelegate.setApplicationLocales()` recrée normalement l'activité active.

Comportement attendu dans Movynex :

- depuis l'écran Réglages, `MainActivity` est recréée et les fragments rechargent les bonnes ressources ;
- `ThisApp` survit, donc l'indexation ne doit pas être redémarrée uniquement à cause du changement de langue ;
- les ViewModels liés à l'activité ou aux fragments sont recréés ;
- les réponses en mémoire sont rechargées dans la nouvelle langue.

Cas à tester spécialement : un changement depuis les réglages système Android pendant que `MPVActivity` est ouverte peut recréer le lecteur. Il faut vérifier la conservation de la position, de la pause, de la piste et du mode Picture-in-Picture. Ne pas ajouter `locale` à `android:configChanges` uniquement pour éviter ce test : cela imposerait une gestion manuelle de toutes les ressources du lecteur.

## Plan d'implémentation conseillé

### Étape 1 — fondation linguistique

- séparer les ressources anglaises et françaises ;
- extraire tous les textes visibles écrits directement dans Kotlin ;
- configurer les langues Android prises en charge ;
- ajouter les ressources de pluriels nécessaires ;
- vérifier la parité des noms de ressources.

### Étape 2 — sélecteur dans Réglages

- ajouter la section et le dialogue ;
- intégrer AppCompat et le stockage Android 12 ;
- prendre en charge Français, English et Système ;
- éviter de stocker la langue dans la session OAuth.

### Étape 3 — TMDB et cache

- introduire le fournisseur de langue ;
- passer explicitement la langue à toutes les requêtes TMDB concernées ;
- conserver la région `FR` séparément ;
- rendre les clés de cache dépendantes de la langue ;
- adapter les dates visibles.

### Étape 4 — données persistantes

- ajouter l'actualisation WorkManager ;
- mettre à jour bibliothèque, historique et JSON hors ligne sans toucher aux vidéos ;
- gérer le changement effectué depuis les réglages système ;
- prévoir le fonctionnement hors connexion.

### Étape 5 — logique et notifications

- corriger `BrowseFragment` pour ne plus dépendre des libellés ;
- traduire les notifications et messages de fond ;
- utiliser un contexte localisé dans les ViewModels, services et Workers.

### Étape 6 — validation et livraison

- ajouter les tests unitaires ;
- compiler les ressources et l'APK debug ;
- valider par pull request si Gradle local reste bloqué ;
- tester manuellement la mise à jour depuis la 1.0.2 sur le téléphone ;
- ne pousser sur `main` et ne produire une release signée qu'après accord explicite.

## Checklist de validation

### Ressources et interface

- [ ] Toutes les ressources par défaut existent en anglais.
- [ ] Toutes les ressources visibles possèdent une version française.
- [ ] Aucun texte utilisateur n'est écrit directement dans Kotlin.
- [ ] Français, English et Système sont proposés dans Réglages.
- [ ] Le choix persiste après fermeture et redémarrage.
- [ ] Movynex apparaît dans les langues par application sur Android 13+.
- [ ] Le changement depuis les réglages système est reflété dans Movynex.

### TMDB et cache

- [ ] Les requêtes françaises envoient `language=fr-FR`.
- [ ] Les requêtes anglaises envoient `language=en-US`.
- [ ] Les requêtes régionales continuent d'envoyer `region=FR`.
- [ ] Une réponse française n'est jamais réutilisée en anglais.
- [ ] Les deux films existants sont actualisés automatiquement.
- [ ] L'absence de traduction TMDB est gérée sans erreur.

### Données utilisateur

- [ ] La connexion Google Drive est conservée.
- [ ] Les dossiers Drive sélectionnés sont conservés.
- [ ] L'historique et les positions de lecture sont conservés.
- [ ] Les fichiers téléchargés sont conservés.
- [ ] Les métadonnées hors ligne sont actualisées sans supprimer les vidéos.
- [ ] Un changement hors connexion est traité au retour du réseau.

### Écrans et services

- [ ] Accueil, Recherche, Bibliothèque et fiches média sont bilingues.
- [ ] Saisons, épisodes, distribution et lecteur sont bilingues.
- [ ] Réglages, OAuth et sélecteur Drive sont bilingues.
- [ ] Téléchargements, indexation et notifications sont bilingues.
- [ ] Les filtres Discover fonctionnent dans les deux langues.
- [ ] Les erreurs et l'écran de plantage utilisent la langue choisie.

### Cycle de vie

- [ ] Le changement à chaud ne lance pas deux indexations.
- [ ] La navigation reste cohérente après recréation de `MainActivity`.
- [ ] Le lecteur conserve ou restaure correctement sa position après une recréation liée à la langue.
- [ ] Le Picture-in-Picture et les actions de notification restent fonctionnels.

### Release

- [ ] Tests unitaires et compilation debug réussis.
- [ ] Test d'installation par-dessus Movynex 1.0.2 réussi.
- [ ] Package `com.cursedcrew.movynex` inchangé.
- [ ] Signature Cursed Crew inchangée.
- [ ] `versionCode` supérieur à 6.
- [ ] Documentation de reprise mise à jour après l'implémentation.

## Décisions à confirmer avant l'implémentation

1. Conserver **Système** comme choix initial recommandé. Sur un téléphone français, l'application restera française.
2. Conserver la région de contenu **France (`FR`)** même lorsque l'interface est anglaise.
3. Actualiser automatiquement les métadonnées persistantes au lieu d'imposer une remise à zéro manuelle du cache.
4. Inclure la localisation complète des écrans actuellement masqués afin qu'ils ne redeviennent pas cassés ou anglais lors d'une réactivation future.
