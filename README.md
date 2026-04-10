# Projet de Synthèse L3 — Langage de script pour animations graphiques simples

## Auteurs

- Senad SENAD

## Présentation générale

Ce projet consiste à développer progressivement un petit langage de script fondé sur des S-expressions pour piloter des éléments graphiques simples dans un espace 2D.  
L’objectif est de pouvoir manipuler dynamiquement des objets graphiques (rectangle, ovale, image, texte), leur associer des commandes, les organiser dans des conteneurs, puis enrichir le système avec des scripts utilisateurs, des expressions, des conditionnelles et des boucles.

Le projet a été réalisé en Java sous Eclipse à partir des briques fournies dans les sujets, en particulier la couche graphique `graphicLayer` et le parseur de S-expressions.

---

## Contenu rendu

### Partie 1

#### Exercice 1
Animation d’un rectangle `robi` dans un `GSpace` :
- déplacement sur les bords internes de la fenêtre ;
- adaptation au redimensionnement ;
- changement périodique de couleur.

#### Exercice 2
Première version de l’interpréteur de script :
- interprétation d’un script composé de plusieurs S-expressions ;
- gestion de commandes simples comme `setColor`, `translate`, `sleep`.

#### Exercice 3
Introduction du modèle par commandes :
- création d’une interface `Command` ;
- séparation de la logique des commandes dans des classes dédiées.

#### Exercice 4
Mise en place d’un environnement de références :
- classe `Environment` pour stocker les références nommées ;
- classe `Reference` pour associer un objet Java à ses commandes ;
- exécution des commandes sans chaîne de conditions centrale ;
- ajout dynamique d’éléments avec `add` ;
- suppression dynamique avec `del`.

#### Exercice 5
Ajout d’éléments dans des conteneurs :
- gestion des noms qualifiés du type `space.robi.im` ;
- support des conteneurs imbriqués ;
- suppression récursive d’un sous-arbre de références.

#### Exercice 6
Scripts utilisateurs :
- ajout de scripts sur une référence avec `addScript` ;
- enregistrement de paramètres ;
- exécution d’un script avec liaison de `self` et des arguments.

#### Exercice 7
Expressions et structures de contrôle :
- opérateurs arithmétiques `+ - * /` ;
- comparaisons `< > =` ;
- affectation avec `set` ;
- conditionnelle `if` ;
- boucle `while` ;
- bloc séquentiel `begin`.

---

## Structure principale du projet

### Package `api`
Le package `api` contient le cœur de l’interpréteur.

- `Environment` : table des références nommées.
- `Reference` : associe un receiver et ses commandes primitives.
- `ValueReference` : encapsule une valeur calculée.
- `Interpreter` : évalue les S-expressions.
- `Command` : interface commune aux commandes.
- `AddElement`, `DelElement` : ajout et suppression dynamique.
- `AddScript`, `ScriptCommand`, `StoredScript`, `ScriptTools` : infrastructure des scripts utilisateurs.
- `GraphicReferenceFactory` : fabrique de références configurées pour les objets graphiques.
- `NewElement`, `NewString`, `NewImage` : création dynamique d’objets.
- `SetColor`, `SetDim`, `Translate`, `ImageTranslate`, `Sleep` : commandes primitives.

### Packages `exerciceX`
Chaque exercice est isolé dans son propre package afin de conserver une progression claire et démontrable.

### Package `tests`
Le package `tests` contient les tests unitaires ciblant surtout la couche API non graphique :
- environnement ;
- références ;
- interpréteur ;
- scripts ;
- logique des noms qualifiés ;
- création dynamique d’éléments.

---

## Choix techniques

### 1. Séparation entre interprétation et exécution
L’interpréteur ne contient pas une grande double conditionnelle `if/else` sur tous les objets et toutes les commandes.  
Le dispatch se fait via :
- `Environment` pour retrouver la référence cible ;
- `Reference` pour retrouver la commande à exécuter.

Cela rend l’architecture plus extensible.

### 2. Références qualifiées
Les objets ajoutés dynamiquement sont enregistrés avec des noms qualifiés :
- `space.robi`
- `space.robi.im`
- `space.robi.label`

Ce mécanisme permet d’avoir plusieurs objets portant le même nom local dans des conteneurs différents tout en évitant les collisions globales.

### 3. Scripts utilisateurs
Les scripts sont stockés comme une signature + une liste d’expressions.  
Lors de l’exécution :
- `self` est lié au nom qualifié du receveur ;
- les autres paramètres sont remplacés par les arguments effectifs ;
- les expressions sont réinjectées dans l’interpréteur standard.

### 4. Tests unitaires
Les tests portent surtout sur la couche non graphique, car elle concentre la logique métier :
- calculs ;
- comparaisons ;
- affectations ;
- boucles ;
- scripts ;
- enregistrement et suppression de références.

---

## Exemples d’utilisation

### Ajout dynamique d’un rectangle
```lisp
(space add robi (Rect new))
(space.robi setColor yellow)
(space.robi translate 50 30)
```

### Ajout d’un élément dans un conteneur
```lisp
(space add robi (Rect new))
(space.robi add eye (Oval new))
(space.robi.eye setColor red)
```

### Script utilisateur
```lisp
(space addScript makeRect (
  (self name size color)
  (self add name (Rect new))
  (self.name setDim size size)
  (self.name setColor color)
))
```

### Expressions et boucle
```lisp
(begin
  (set i 0)
  (while (< i 5)
    (set i (+ i 1))
  )
  i
)
```

### Partie 2 — Application distribuée + IHM

Architecture client–serveur :
- Serveur = interpréteur (réutilisation complète de la partie 1)
- Client = interface graphique + envoi de commandes
- Communication via objets sérialisés (requêtes / réponses)

 Fonctionnalités implémentées :
- Envoi de commandes depuis le client
- Exécution côté serveur
- Retour de l’état graphique (SceneData)
- Synchronisation client ↔ serveur

Interface graphique (Swing) :
- Visualisation de la scène
- Interaction via boutons (add, move, etc.)
- Console de commandes
- Arbre des éléments

 Persistance :
- Sauvegarde / chargement de scènes (sérialisation)

---