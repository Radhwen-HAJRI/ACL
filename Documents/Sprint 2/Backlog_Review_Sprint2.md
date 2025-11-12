## Sprint 2 Review


## Objectifs

Transformer le prototype en un véritable jeu fonctionnel en implémentant les mécaniques de base : système de PV, IA des monstres, sons, score, et conditions de victoire/défaite.

## Fonctionnalités réalisées

* Mise en place du système de points de vie (3 PV) pour le héros.
* Amélioration de l'IA des monstres (poursuite active et attaque à proximité).
* Implémentation du SoundManager (sons de victoire, défaite, etc.).
* Activation de la condition de victoire ("Jeu Gagné").
* Ajout d'un système de score via la collecte de "coins".
* Ajout d'un système de combat permettant au héros d'attaquer et de tuer les monstres.
* Implémentation d'une caméra dynamique qui suit le joueur.
* Implémentation des tests unitaires automatisés (JUnit) pour valider les fonctionnalités des classes Player et Monster. Tous les tests (12 au total) passent avec succès lors du build mvn package


## Fonctionnalités non réalisées

* L'option de "Rejouer" (Reset) après une victoire ou une défaite (reportée au sprint 3).
* Le système de niveaux multiples et de difficulté (reporté au sprint 3).
* L'affichage du temps de jeu (reporté au sprint 3).

## Bilan
La majorité des objectifs critiques du sprint ont été atteints. Le jeu est désormais une expérience fonctionnelle et interactive, dépassant même les attentes initiales avec l'ajout du système de combat et d'une caméra dynamique. Le projet est stable et prêt pour l'ajout des fonctionnalités finales.