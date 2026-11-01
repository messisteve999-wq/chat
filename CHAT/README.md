# CHAT — Assistant IA flottant

Projet Android Kotlin indépendant, conçu pour être compilé avec GitHub Actions.

## Fonctions de cette base
- Icône Android ronde avec motif dragon stylisé noir/rouge (vectoriel de départ).
- Écran d'accueil avec accès aux réglages de notifications et de superposition.
- Service de bulle flottante activable par l'utilisateur.
- `NotificationListenerService` déclaré avec les métadonnées de filtre.
- Réception locale des notifications WhatsApp et WhatsApp Business.
- Historique local minimal des notifications observées.
- Appel au serveur Gemini-Server-Steve via `POST /v1/generate`.
- Workflow GitHub Actions pour produire un APK debug.

## Limites importantes
Android ne donne pas automatiquement à une application l'accès à toute la base de conversations WhatsApp. Cette base enregistre les notifications accessibles et les textes disponibles. Les notifications peuvent ne pas contenir les messages complets. L'envoi automatique dépend des actions de réponse réellement exposées par chaque notification et n'est pas garanti pour tous les messages.

L'autorisation d'accès aux notifications et l'autorisation d'affichage par-dessus les autres applications doivent être accordées manuellement dans les paramètres Android. Une application ne peut pas s'accorder ces accès elle-même.

## Compilation
Importer le dépôt sur GitHub, puis lancer l'action **Build CHAT APK**. L'APK est publié comme artefact `CHAT-debug-APK`.

Configuration du SDK : compileSdk 35, minSdk 23, targetSdk 28.
