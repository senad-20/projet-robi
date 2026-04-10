# Plan de test

Ce document liste les classes de test du package `tests` et le rôle de chacune.

## Organisation

Les tests sont regroupés dans le package `tests`.  
Chaque fichier de test correspond à une classe importante du package `api`.

---

## `EnvironmentTest`

Teste la gestion de l’environnement d’exécution :

- création d’un environnement vide ;
- ajout d’une référence avec `addReference` ;
- récupération d’une référence avec `getReferenceByName` ;
- erreur si la référence n’existe pas ;
- remplacement d’une référence portant le même nom ;
- suppression simple avec `removeReference` ;
- suppression récursive d’un sous-arbre avec `removeReferenceTree` ;
- vérification que les références hors du sous-arbre ne sont pas supprimées.

---

## `ReferenceTest`

Teste le fonctionnement des références :

- stockage du receiver ;
- affectation et lecture du nom ;
- ajout d’une commande avec `addCommand` ;
- vérification avec `hasCommand` ;
- exécution d’une commande avec `run` ;
- erreur si la commande demandée n’existe pas ;
- vérification que l’expression complète est bien transmise à la commande.

---

## `ValueReferenceTest`

Teste les références de valeur :

- stockage d’un entier ;
- stockage d’un booléen ;
- stockage d’une chaîne ;
- stockage de `null` ;
- accès à la valeur via `getReceiver` et `getValue`.

---

## `InterpreterTest`

Teste l’interpréteur :

- affectation avec `set` ;
- calculs avec `+`, `-`, `*`, `/` ;
- comparaisons avec `<`, `>`, `=` ;
- évaluation de variables ;
- exécution de `if` ;
- exécution de `while` ;
- exécution de `begin` ;
- expressions imbriquées ;
- mise à jour de l’environnement ;
- erreur si un entier est attendu mais absent ;
- erreur si un booléen est attendu mais absent ;
- délégation vers une référence connue dans l’environnement.

---

## `ScriptToolsTest`

Teste l’instanciation des scripts :

- remplacement simple d’un paramètre ;
- remplacement de plusieurs paramètres ;
- remplacement dans une expression imbriquée ;
- non-remplacement dans un identifiant plus long ;
- conservation des tokens non remplacés ;
- conservation de la structure de l’expression.

---

## `AddScriptTest`

Teste l’enregistrement d’un script utilisateur :

- ajout d’un script valide ;
- vérification que le script devient une commande ;
- retour correct du receiver ;
- erreur si la définition du script est absente ;
- erreur si la définition est vide ;
- erreur si le premier paramètre n’est pas `self` ;
- acceptation d’un script avec seulement `self` ;
- acceptation d’un script avec `self` et d’autres paramètres.

---

## `ScriptCommandTest`

Teste l’exécution d’un script enregistré :

- vérification du nombre d’arguments ;
- liaison correcte de `self` ;
- liaison correcte des paramètres ;
- exécution de plusieurs expressions dans le corps du script ;
- retour du receiver après exécution ;
- utilisation d’expressions de l’interpréteur dans un script ;
- vérification que les remplacements ne cassent pas les identifiants plus longs ;
- appels successifs avec des arguments différents.

---

## `StoredScriptTest`

Teste le stockage interne d’un script :

- stockage des noms de paramètres ;
- stockage des expressions du corps ;
- lecture correcte via les getters.

---

## `AddElementTest`

Teste l’ajout dynamique d’un élément graphique :

- ajout d’un élément dans `space` ;
- création d’une référence qualifiée ;
- retour de la référence nouvellement créée ;
- ajout dans un conteneur imbriqué ;
- remplacement d’une référence si le même nom qualifié est réutilisé ;
- erreur si le receiver n’est pas un conteneur ;
- erreur si l’expression de création ne retourne pas un élément graphique ;
- erreur si la fabrique demandée n’existe pas.

---

## `DelElementTest`

Teste la suppression dynamique :

- suppression d’un élément simple ;
- suppression récursive d’un sous-arbre ;
- conservation des branches sœurs ;
- retour du receiver après suppression ;
- erreur si le receiver n’est pas un conteneur ;
- erreur si la cible à supprimer n’existe pas ;
- erreur si la cible n’est pas un élément graphique.

---

## `GraphicReferenceFactoryTest`

Teste la configuration automatique des références graphiques :

- présence des commandes attendues pour un conteneur borné ;
- absence de commandes `add` / `del` pour un élément non conteneur ;
- création correcte d’une référence pour image ;
- vérification des commandes disponibles selon le type créé.

---

## `NewElementTest`

Teste la création dynamique d’éléments graphiques standards :

- création d’un `GRect` ;
- création d’un `GOval` ;
- présence des commandes configurées sur la référence créée ;
- erreur si le receiver n’est pas une classe valide.

---

## `NewStringTest`

Teste la création dynamique d’un élément texte :

- création d’un élément texte avec constructeur `String` ;
- présence des commandes configurées ;
- erreur si la classe ne possède pas le bon constructeur ;
- erreur si le receiver n’est pas une classe valide.

---
