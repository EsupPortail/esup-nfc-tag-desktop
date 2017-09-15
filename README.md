ESUP-NFC-TAG-DESKTOP
===============

Esup-nfc-tag-desktop permet d'encoder et de lire les cartes Mifare Desfire EV1. 
Le client s'appuit sur la platefome Esup-nfc-tag qui calcule les commandes (APDU) à transmettre à la carte.

L'application est packagée sous la forme d'un jar comprenant les dépendences : esupnfctagdesktop-1.0-SNAPSHOT-jar-with-dependencies.jar


## Fonctionalités

1 - L'application esup-nfc-tag-desktop se comporte de la même manière que l'application Android esup-nfc-tag-droid
2 - L'application repose sur un composant webview JavaFX qui se connecte sur esup-nfc-tag-server
3 - Après l'authentification Shibboleth il faut choisir la salle de badgeage
4 - Pour badger il suffit de poser une carte sur le lecteur nfc

## Environnement

### Logiciel

L'application est prévue pour tourner avec java 8.

### Materiel

- un lecteur de carte compatible PC/SC (ex: Indentive Cloud 4700f, OMNIKEY CardMan 5x21-CL...)

## Compilation esup-nfc-client

Modifier la classe principale pour changer l'adresse url du serveur esup-nfc-tag-server
Dans le dossier esup-nfc-client executer : mvn clean package


