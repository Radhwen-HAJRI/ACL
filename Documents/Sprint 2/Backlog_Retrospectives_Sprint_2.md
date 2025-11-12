# Rétrospective du Sprint 2


## Objectif du sprint

L'objectif principal du Sprint 2 était de transformer notre prototype en un véritable jeu fonctionnel. Selon notre backlog, nous devions implémenter les mécaniques de base :

* Un système de points de vie (PV) pour le héros.
* Une condition de victoire à l'atteinte du trésor.
* Un affichage HUD pour les PV et un score.
* Des sons basiques pour les actions principales.
* Une amélioration de l'IA des monstres.
* La préparation d'un système de niveaux multiples.
* Ajout d'un système de score.

## Résultats obtenus

Nous avons réussi à implémenter la majorité des fonctionnalités critiques prévues et même à en dépasser certaines.

Fonctionnalités du backlog terminées :

* Le système de points de vie (3 PV) est fonctionnel.
* L'IA des monstres a été améliorée (ils poursuivent maintenant le joueur et à l'approche du Héros , les monstres commecent à l'attaquer).
* Le gestionnaire de sons (SoundManager) est en place et les sons de victoire/défaite sont joués.
* La condition de victoire ("Jeu Gagné") est active lorsque le joueur atteint la fin.
* Collecte des "coins" réparties sur l'aire du jeu et affichage du score après chaque collecte.

Fonctionnalités ajoutées (non prévues) :

* Système de combat : La plus grosse addition. Le héros peut désormais attaquer les monstres (3 coups pour tuer) au lieu de simplement les éviter.
* Caméra dynamique : Nous avons amélioré la carte pour qu'elle soit "mobile" (la caméra suit le joueur), ce qui rend le visuel plus dynamique et le monde plus grand.
* Implémentation des tests unitaires automatisés (JUnit) pour valider les fonctionnalités des classes Player et Monster. Tous les tests (12 au total) passent avec succès lors du build mvn package

Fonctionnalités reportées :

Certaines fonctionnalités ont été reportées pour le prochain sprint :

* L'option de "Rejouer" (Reset) après une victoire ou une défaite.
* Le système de niveaux multiples (facile/difficile).
* Affichage du temps joué par le Héros depuis le début du jeu.

## Points positifs

Excellente gestion du temps et répartition équitable des tâches. Le travail a été très fluide.

* Grande flexibilité de l'équipe : Nous avons constaté que la répartition initiale des tâches n'était pas toujours optimale pour la fluidité. Les contributeurs ont échangé des tâches (l'un faisant le travail assigné à l'autre, et vice-versa) pour éviter les blocages, ce qui a très bien fonctionné.

* Capacité d'adaptation : Nous avons collectivement décidé d'ajouter le système de combat, une fonctionnalité majeure non prévue, car elle semblait plus importante pour le "fun" que le système de score.

## Points à améliorer

Planification : L'ajout de fonctionnalités majeures (comme le combat) en plein sprint, bien que positif, a logiquement consommé le temps alloué à d'autres tâches (comme le système de niveaux ou le reset ).


Backlog : Le backlog initial était peut-être un peu trop ambitieux. Nous devons mieux estimer l'impact des "nouvelles idées" sur le planning.

## Décisions pour le sprint suivant

* Prioriser l'implémentation de la fonctionnalité "Rejouer" (Reset) après une victoire ou une défaite.
* Commencer le développement du système de niveaux multiples, en définissant différents niveaux de difficulté.
* Affichage du nombre de monstres tués par le joueur.