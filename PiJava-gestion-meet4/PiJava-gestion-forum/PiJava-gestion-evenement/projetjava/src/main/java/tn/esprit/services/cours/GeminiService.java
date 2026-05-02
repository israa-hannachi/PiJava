package tn.esprit.services.cours;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Service avancé pour interagir avec l'IA Google Gemini.
 * Capable de lire le texte HTML et d'extraire le contenu des fichiers PDF (locaux ou Cloudinary).
 *
 * Supporte le fallback automatique entre plusieurs modèles Gemini
 * en cas d'épuisement du quota d'un modèle, avec retry automatique.
 */
public class GeminiService {

    private static GeminiService instance;
    private final ObjectMapper objectMapper;
    private final Properties config;

    // Clé API Gemini (chargée depuis le fichier de configuration)
    private String apiKey;

    // URL de base pour l'API Gemini (le nom du modèle sera injecté dynamiquement)
    private String getApiBaseUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=" + apiKey;
    }

    /**
     * Liste ordonnée des modèles à essayer en fallback.
     * Si le premier échoue (quota épuisé), on passe au suivant, etc.
     */
    private static final String[] MODELS = {
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-pro"
    };

    /** Nombre maximal de tentatives par modèle en cas d'erreur de quota temporaire. */
    private static final int MAX_RETRIES_PER_MODEL = 2;

    /** Délai d'attente initial entre les retries (en millisecondes). */
    private static final long RETRY_DELAY_MS = 5000;

    private GeminiService() {
        this.objectMapper = new ObjectMapper();
        this.config = new Properties();
        loadConfiguration();
    }

    /**
     * Charge la configuration depuis le fichier config.properties
     */
    private void loadConfiguration() {
        try {
            // Essayer de charger depuis les resources
            InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (configStream == null) {
                // Fallback: essayer un chemin relatif
                configStream = new FileInputStream("src/main/resources/config.properties");
            }
            
            if (configStream != null) {
                config.load(configStream);
                configStream.close();
                
                // Charger la clé API
                this.apiKey = getConfigValue(config, "GEMINI_API_KEY");
                
                if (apiKey.isEmpty() || apiKey.equals("")) {
                    System.err.println("⚠️ [CONFIG] Veuillez mettre à jour votre clé API dans config.properties");
                    System.err.println("   Obtenez une nouvelle clé gratuite sur: https://aistudio.google.com/apikey");
                } else {
                    System.out.println("✅ [CONFIG] Clé API Gemini chargée avec succès");
                }
            } else {
                throw new RuntimeException("Fichier config.properties non trouvé");
            }
        } catch (Exception e) {
            System.err.println("❌ [CONFIG] Erreur lors du chargement de la configuration: " + e.getMessage());
            // Fallback vers l'ancienne clé codée en dur (temporaire)
            this.apiKey = "";
        }
    }

    public static synchronized GeminiService getInstance() {
        if (instance == null) {
            instance = new GeminiService();
        }
        return instance;
    }

    /**
     * Met à jour la clé API Gemini (utile pour changer de clé sans redémarrer l'application)
     */
    public void updateApiKey(String newApiKey) {
        if (newApiKey != null && !newApiKey.trim().isEmpty() && !newApiKey.equals("")) {
            this.apiKey = newApiKey.trim();
            System.out.println("✅ [CONFIG] Clé API Gemini mise à jour avec succès");
            
            // Optionnel: sauvegarder dans le fichier de configuration
            try {
                config.setProperty("GEMINI_API_KEY", this.apiKey);
                // Note: la sauvegarde nécessiterait un accès en écriture au fichier
            } catch (Exception e) {
                System.err.println("⚠️ [CONFIG] Impossible de sauvegarder la nouvelle clé: " + e.getMessage());
            }
        } else {
            System.err.println("❌ [CONFIG] Clé API invalide ou inchangée");
        }
    }

    /**
     * Vérifie si la clé API actuelle est valide (non vide et différente de l'ancienne clé expirée)
     */
    public boolean isApiKeyValid() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("");
    }

    private String getConfigValue(Properties config, String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }
        return config.getProperty(key, "").trim();
    }

    /**
     * Génère un résumé en combinant le contenu HTML et le contenu du fichier PDF si présent.
     */
    public String generateSummary(String htmlContent, String pdfUrl) {
        // Vérifier si la clé API est valide
        if (!isApiKeyValid()) {
            return "⚠️ Erreur de configuration : La clé API Gemini n'est pas valide.\n\n" +
                   "Veuillez mettre à jour votre clé API dans le fichier config.properties :\n" +
                   "1. Obtenez une nouvelle clé gratuite sur https://aistudio.google.com/apikey\n" +
                   "2. Remplacez la valeur de GEMINI_API_KEY dans src/main/resources/config.properties\n" +
                   "3. Redémarrez l'application";
        }

        StringBuilder combinedText = new StringBuilder();

        // 1. Nettoyer et ajouter le contenu HTML
        if (htmlContent != null && !htmlContent.trim().isEmpty()) {
            String plainText = htmlContent.replaceAll("<[^>]*>", " ").trim();
            if (!plainText.isEmpty()) {
                combinedText.append("CONTENU TEXTUEL DU COURS :\n").append(plainText).append("\n\n");
            }
        }

        // 2. Extraire et ajouter le contenu PDF
        if (pdfUrl != null && !pdfUrl.trim().isEmpty()) {
            try {
                System.out.println("🚀 [INFO] Tentative d'extraction PDF depuis : " + pdfUrl);
                String pdfText = extractTextFromPdf(pdfUrl);
                if (pdfText != null && !pdfText.trim().isEmpty()) {
                    combinedText.append("CONTENU DU FICHIER PDF ASSOCIÉ :\n").append(pdfText);
                }
            } catch (Exception e) {
                System.err.println("❌ [ERREUR] Extraction PDF échouée : " + e.getMessage());
            }
        }

        String finalPromptContent = combinedText.toString().trim();
        if (finalPromptContent.isEmpty()) {
            return "⚠️ Impossible de générer un résumé : ce cours ne contient ni texte ni PDF.\n\n" +
                   "Solution : Ajoutez du contenu texte ou un fichier PDF au cours.";
        }

        String fullPrompt = "Tu es un assistant pédagogique expert. " +
                "Voici les sources de contenu d'un cours. " +
                "Peux-tu en faire un résumé structuré, clair et pédagogique ?\n\n" +
                finalPromptContent;

        System.out.println("📤 [INFO] Envoi de la requête à Gemini (" + finalPromptContent.length() + " caractères)");
        return callGeminiApiWithFallback(fullPrompt);
    }

    /**
     * Extrait le texte d'un PDF depuis une URL (Cloudinary) ou un chemin local.
     */
    private String extractTextFromPdf(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("http")) {
            // Cas URL Distante (Cloudinary)
            URL url = new URL(pathOrUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            try (InputStream is = connection.getInputStream();
                 PDDocument document = Loader.loadPDF(IOUtils.toByteArray(is))) {
                return new PDFTextStripper().getText(document);
            } finally {
                connection.disconnect();
            }
        } else {
            // Cas Fichier Local
            File file = new File(pathOrUrl);
            if (file.exists()) {
                try (PDDocument document = Loader.loadPDF(file)) {
                    return new PDFTextStripper().getText(document);
                }
            }
        }
        return null;
    }

    /**
     * Appelle l'API Gemini avec un système de fallback multi-modèles.
     * Essaie chaque modèle dans l'ordre, avec retry en cas d'erreur de quota temporaire.
     */
    private String callGeminiApiWithFallback(String prompt) {
        List<String> errors = new ArrayList<>();

        for (String model : MODELS) {
            System.out.println("🤖 [INFO] Tentative avec le modèle : " + model);

            for (int attempt = 1; attempt <= MAX_RETRIES_PER_MODEL; attempt++) {
                ApiResult result = callGeminiApi(prompt, model);

                if (result.success) {
                    System.out.println("✅ [INFO] Réponse reçue avec succès du modèle : " + model);
                    return result.content;
                }

                // Vérifier si c'est une erreur de quota
                if (result.isQuotaError) {
                    System.out.println("⚠️ [QUOTA] Modèle " + model + " — quota épuisé (tentative " + attempt + "/" + MAX_RETRIES_PER_MODEL + ")");

                    // Si la limite est "0" (quota dur), pas la peine de retry — passer au modèle suivant
                    if (result.content.contains("limit: 0")) {
                        System.out.println("🚫 [QUOTA] Limite = 0 pour " + model + ", passage au modèle suivant...");
                        errors.add(model + " : quota épuisé (limite = 0)");
                        break; // Sortir de la boucle retry, passer au modèle suivant
                    }

                    // Sinon, c'est un rate-limit temporaire → retry avec délai
                    if (attempt < MAX_RETRIES_PER_MODEL) {
                        long waitMs = RETRY_DELAY_MS * attempt;
                        System.out.println("⏳ [RETRY] Attente de " + (waitMs / 1000) + "s avant la prochaine tentative...");
                        try {
                            Thread.sleep(waitMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return "Génération interrompue.";
                        }
                    } else {
                        errors.add(model + " : quota temporairement épuisé après " + MAX_RETRIES_PER_MODEL + " tentatives");
                    }
                } else {
                    // Erreur non liée au quota → conserver l'erreur et passer au modèle suivant
                    errors.add(model + " : " + result.content);
                    break; // Pas la peine de retry pour une erreur non-quota
                }
            }
        }

        // Tous les modèles ont échoué
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("⚠️ Tous les modèles Gemini ont échoué.\n\n");
        errorMsg.append("Solutions possibles :\n");
        errorMsg.append("• Attendez quelques minutes et réessayez\n");
        errorMsg.append("• Vérifiez votre clé API sur https://ai.google.dev/\n");
        errorMsg.append("• Générez une nouvelle clé API gratuite sur https://aistudio.google.com/apikey\n\n");
        errorMsg.append("Détails des erreurs :\n");
        for (String err : errors) {
            errorMsg.append("  - ").append(err).append("\n");
        }
        return errorMsg.toString();
    }

    /**
     * Appelle l'API Gemini pour un modèle spécifique.
     * Renvoie un objet ApiResult contenant le résultat ou l'erreur.
     */
    private ApiResult callGeminiApi(String prompt, String model) {
        String apiUrl = String.format(getApiBaseUrl(), model);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);

            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> body = new HashMap<>();
            body.put("contents", Collections.singletonList(content));

            String jsonBody = objectMapper.writeValueAsString(body);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // Succès : le modèle a renvoyé des candidats
                if (rootNode.has("candidates") && rootNode.get("candidates").isArray() && rootNode.get("candidates").size() > 0) {
                    JsonNode candidate = rootNode.get("candidates").get(0);
                    if (candidate.has("content") && candidate.get("content").has("parts")) {
                        String text = candidate.get("content").get("parts").get(0).get("text").asText();
                        return ApiResult.success(text);
                    }
                }

                // Erreur API
                if (rootNode.has("error")) {
                    String errorMessage = rootNode.get("error").get("message").asText();
                    boolean isQuota = errorMessage.toLowerCase().contains("quota")
                            || errorMessage.toLowerCase().contains("rate")
                            || errorMessage.toLowerCase().contains("exceeded");
                    return ApiResult.error(errorMessage, isQuota);
                }

                return ApiResult.error("Réponse inattendue : " + responseBody, false);
            }
        } catch (Exception e) {
            return ApiResult.error("Erreur de communication : " + e.getMessage(), false);
        }
    }

    /**
     * Objet interne pour encapsuler le résultat d'un appel API.
     */
    private static class ApiResult {
        final boolean success;
        final boolean isQuotaError;
        final String content;

        private ApiResult(boolean success, String content, boolean isQuotaError) {
            this.success = success;
            this.content = content;
            this.isQuotaError = isQuotaError;
        }

        static ApiResult success(String content) {
            return new ApiResult(true, content, false);
        }

        static ApiResult error(String message, boolean isQuotaError) {
            return new ApiResult(false, message, isQuotaError);
        }
    }
}
