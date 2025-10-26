# SMTP Mock

Application complète permettant de simuler un serveur SMTP pour vos tests. Le backend est développé avec Spring Boot (Java 8) et embarque un serveur SMTP d'écoute sur le port `2525`. Chaque message reçu est stocké en mémoire puis exposé via une API REST. Le frontend Angular affiche ces messages dans une interface agréable et offre la possibilité de les supprimer ou de vider complètement la boîte.

## Prérequis

- Java 8+
- Maven 3.9+
- Node.js 18+ (ou supérieur)
- npm 9+

## Démarrage du backend

```bash
cd backend
mvn spring-boot:run
```

Le serveur HTTP écoute par défaut sur le port `8080` et le serveur SMTP mock sur `2525`. Vous pouvez modifier ces ports dans `backend/src/main/resources/application.yml`.

## Démarrage du frontend

Installez les dépendances puis lancez le serveur de développement :

```bash
cd frontend
npm install
npm start
```

Le serveur Angular est configuré avec un proxy (`proxy.conf.json`) qui redirige les appels `/api` vers `http://localhost:8080`.

## Utilisation

Configurez votre application cliente pour envoyer ses e-mails vers `localhost` sur le port `2525`. Chaque message apparaîtra instantanément dans l'interface web. Vous pouvez :

- Actualiser la liste des messages reçus
- Supprimer un message individuel
- Vider entièrement la liste des messages
- Consulter le contenu analysé et le message brut

## Tests

- Backend : `mvn test`
- Frontend : `npm test`

Ces commandes nécessitent l'installation préalable des dépendances.
