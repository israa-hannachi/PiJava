# Configuration de l'API Gemini pour la génération de résumés

## Problème actuel
L'application utilise une clé API Gemini qui a épuisé son quota, ce qui empêche la génération automatique des résumés.

## Solution

### 1. Obtenir une nouvelle clé API
1. Allez sur [Google AI Studio](https://aistudio.google.com/apikey)
2. Connectez-vous avec votre compte Google
3. Cliquez sur "Create API Key"
4. Copiez la nouvelle clé générée (elle commence par "AIzaSy...")

### 2. Mettre à jour la configuration
1. Ouvrez le fichier : `src/main/resources/config.properties`
2. Remplacez la ligne :
   ```
   GEMINI_API_KEY=AIzaSyDYsaElVpItsnMrJ5RAhDg7J55TwCxmCSY
   ```
   par :
   ```
   GEMINI_API_KEY=VOTRE_NOUVELLE_CLE_API_ICI
   ```

### 3. Redémarrer l'application
Après avoir mis à jour la clé, redémarrez l'application pour que les changements prennent effet.

## Fonctionnalités implémentées

### ✅ Système de configuration externe
- La clé API n'est plus codée en dur
- Chargement depuis `config.properties`
- Messages d'erreur clairs si la clé est invalide

### ✅ Validation automatique
- Vérification que la clé n'est pas vide
- Détection de l'ancienne clé expirée
- Messages d'aide pour l'utilisateur

### ✅ Fallback multi-modèles
- Essai automatique sur plusieurs modèles Gemini
- Retry en cas d'erreur temporaire
- Gestion intelligente des erreurs de quota

### ✅ Mise à jour dynamique (optionnel)
```java
// Pour mettre à jour la clé sans redémarrer
GeminiService service = GeminiService.getInstance();
service.updateApiKey("NOUVELLE_CLE_API");
```

## Modèles supportés (par ordre de priorité)
1. `gemini-2.0-flash` (plus rapide)
2. `gemini-1.5-flash` 
3. `gemini-1.5-pro` (plus capable)
4. `gemini-pro` (legacy)

## Dépannage

### Si l'erreur persiste
1. Vérifiez que la nouvelle clé API est correctement copiée (sans espaces)
2. Assurez-vous que le projet Google AI Studio est actif
3. Vérifiez que vous n'avez pas dépassé les quotas de la nouvelle clé

### Logs utiles
L'application affiche des logs détaillés :
- `✅ [CONFIG] Clé API Gemini chargée avec succès`
- `⚠️ [CONFIG] Veuillez mettre à jour votre clé API`
- `🤖 [INFO] Tentative avec le modèle : gemini-2.0-flash`

## Coûts
L'API Gemini Free tier inclut :
- 60 requêtes/minute pour les modèles Flash
- 15 requêtes/minute pour les modèles Pro
- Quotas généreux pour le développement

Pour une utilisation intensive, envisagez de passer à un plan payant.
