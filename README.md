Message Manager
===============

# Principe

Le message manager a pour but de faciliter le cycle de vie des messages reçus ou envoyés.

Lors de l'intégration ou de la constitution d'un message, il arrive que des erreurs soient remontées.
Afin de traiter chaque erreur, il dispose d'un dictionnaire d'erreurs où chaque type d'erreur est associée à une [typologie de recyclage](https://gitlab.talanlabs.com/nicolas-poste/message-manager/wikis/home#typologie-des-types-de-recyclage-induites-par-une-erreur).

# Dépendance

```xml
		<dependency>
			<groupId>message-manager</groupId>
			<artifactId>message-manager</artifactId>
			<version>1.1.2</version>
		</dependency>
```

# Vocabulaire

[Voir wiki](https://gitlab.talanlabs.com/nicolas-poste/message-manager/wikis/home#d%C3%A9finitions)

# Architecture

## Projet message-manager-shared

Contient la définition des différentes interfaces et modèles représentant les messages, les erreurs, les domaines, ...

## Projet message-manager-engine

A pour dépendance le projet *shared*

Contient le dictionnaire et le moteur du message manager. Contient également l'interface que tout process doit implémenter.

## Projet message-manager-server

A pour dépendance le projet *engine*

A pour dépendance le process manager de TalanLabs, et propose une implémentation par défaut pour un process : `AbstractMMAgent` ainsi qu'une implémentation par défaut pour un injecteur : `AbstractMMInjector`.

[Cf documentation sur le wiki](https://gitlab.talanlabs.com/nicolas-poste/message-manager/wikis/home#workflow-dun-agent)

![mmagent](/uploads/51304f04824113232e751a2ff6972386/mmagent.png)

## Projet message-manager-supervision

A pour dépendance le projet *server*

Rajoute au serveur une classe pour superviser (`SupervisionUtils`), qui retourne la liste des channels et de leur état/occupation.

## Projet message-manager-test-dependencies

Projet **POM**

Rajoute jetty, ainsi que les dépendances *test-jar* du serveur et du moteur. A utiliser en scope *test* dans votre projet.

# Dépendance
```xml
        <dependency>
            <groupId>com.talanlabs</groupId>
            <artifactId>message-manager-supervision</artifactId>
            <version>...</version>
        </dependency>
```

# Documentation

[Voir wiki](https://gitlab.talanlabs.com/nicolas-poste/message-manager/wikis/home)

# Faire une release

Faire simplement un tag, GitLab-CI fera une release automatiquement avec le nom du tag
