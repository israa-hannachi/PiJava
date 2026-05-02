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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Service gratuit et rapide utilisant Groq (Llama 3.1)
 * Groq offre un généreux quota gratuit et est très rapide
 */
public class GroqService {
    
    private static GroqService instance;
    private final ObjectMapper objectMapper;
    private String apiKey;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    private GroqService() {
        this.objectMapper = new ObjectMapper();
        loadConfiguration();
    }
    
    public static synchronized GroqService getInstance() {
        if (instance == null) {
            instance = new GroqService();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis le fichier config.properties
     */
    private void loadConfiguration() {
        try {
            Properties config = new Properties();
            java.io.InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (configStream == null) {
                configStream = new java.io.FileInputStream("src/main/resources/config.properties");
            }
            
            if (configStream != null) {
                config.load(configStream);
                configStream.close();
                this.apiKey = getConfigValue(config, "GROQ_API_KEY");
                
                if (!apiKey.isEmpty()) {
                    System.out.println("✅ [CONFIG] Clé API Groq chargée");
                } else {
                    System.out.println("ℹ️ [CONFIG] Clé API Groq non configurée");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [CONFIG] Erreur chargement Groq: " + e.getMessage());
            this.apiKey = "";
        }
    }
    
    /**
     * Génère un résumé en utilisant Groq Llama 3.1
     */
    public String generateSummary(String htmlContent, String pdfUrl) {
        if (apiKey.isEmpty()) {
            return getSetupInstructions();
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
        
        String prompt = "Tu es un assistant pédagogique expert. Voici les sources de contenu d'un cours. Résume en français ce contenu de manière claire et structurée :\n\n" + finalPromptContent;
        
        System.out.println("📤 [INFO] Envoi de la requête à Groq Llama 3.1");
        return callGroq(prompt);
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
            java.io.File file = new java.io.File(pathOrUrl);
            if (file.exists()) {
                try (PDDocument document = Loader.loadPDF(file)) {
                    return new PDFTextStripper().getText(document);
                }
            }
        }
        return null;
    }
    
    /**
     * Appelle l'API Groq
     */
    private String callGroq(String prompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            
            // Construire le corps de la requête (format OpenAI-compatible)
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.1-8b-instant");
            body.put("messages", Collections.singletonList(message));
            body.put("max_tokens", 1000);
            body.put("temperature", 0.7);
            
            String jsonBody = objectMapper.writeValueAsString(body);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(responseBody);
                
                if (rootNode.has("choices") && rootNode.get("choices").isArray() && 
                    rootNode.get("choices").size() > 0) {
                    JsonNode choice = rootNode.get("choices").get(0);
                    if (choice.has("message") && choice.get("message").has("content")) {
                        String content = choice.get("message").get("content").asText();
                        System.out.println("✅ [INFO] Résumé généré avec succès par Groq");
                        return content;
                    }
                }
                
                // Erreur API
                if (rootNode.has("error")) {
                    String errorMessage = rootNode.get("error").get("message").asText();
                    return "Erreur Groq: " + errorMessage;
                }
                
                return "Réponse inattendue de Groq: " + responseBody;
            }
        } catch (Exception e) {
            return "Erreur de communication Groq: " + e.getMessage();
        }
    }
    
    /**
     * Retourne les instructions pour configurer Groq
     */
    private String getSetupInstructions() {
        return "⚠️ Groq nécessite une clé API (gratuite).\n\n" +
               "Pour configurer Groq :\n" +
               "1. Allez sur https://console.groq.com/keys\n" +
               "2. Créez un compte gratuit\n" +
               "3. Générez une clé API\n" +
               "4. Ajoutez-la dans config.properties :\n" +
               "   GROQ_API_KEY=votre_clé_groq\n\n" +
               "Groq offre un généreux quota gratuit avec Llama 3.1 !";
    }
    
    /**
     * Met à jour la clé API Groq
     */
    public void updateApiKey(String newApiKey) {
        if (newApiKey != null && !newApiKey.trim().isEmpty()) {
            this.apiKey = newApiKey.trim();
            System.out.println("✅ [CONFIG] Clé API Groq mise à jour");
        }
    }
    
    /**
     * Vérifie si Groq est configuré
     */
    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    private String getConfigValue(Properties config, String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }
        return config.getProperty(key, "").trim();
    }
}
