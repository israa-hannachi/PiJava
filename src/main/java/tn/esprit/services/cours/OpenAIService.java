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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Service alternatif pour la génération de résumés utilisant OpenAI GPT
 * Fallback lorsque Gemini est indisponible
 */
public class OpenAIService {
    
    private static OpenAIService instance;
    private final ObjectMapper objectMapper;
    private String apiKey;
    
    private OpenAIService() {
        this.objectMapper = new ObjectMapper();
        loadConfiguration();
    }
    
    public static synchronized OpenAIService getInstance() {
        if (instance == null) {
            instance = new OpenAIService();
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
                this.apiKey = config.getProperty("OPENAI_API_KEY", "");
                
                if (!apiKey.isEmpty()) {
                    System.out.println("✅ [CONFIG] Clé API OpenAI chargée");
                } else {
                    System.out.println("⚠️ [CONFIG] Clé API OpenAI non configurée");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [CONFIG] Erreur chargement OpenAI: " + e.getMessage());
            this.apiKey = "";
        }
    }
    
    /**
     * Génère un résumé en utilisant OpenAI GPT
     */
    public String generateSummary(String htmlContent, String pdfUrl) {
        if (apiKey.isEmpty()) {
            return "⚠️ OpenAI non configuré. Ajoutez OPENAI_API_KEY dans config.properties";
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
        
        String prompt = "Tu es un assistant pédagogique expert. " +
                "Voici les sources de contenu d'un cours. " +
                "Peux-tu en faire un résumé structuré, clair et pédagogique ?\n\n" +
                finalPromptContent;
        
        System.out.println("📤 [INFO] Envoi de la requête à OpenAI GPT");
        return callOpenAI(prompt);
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
     * Appelle l'API OpenAI
     */
    private String callOpenAI(String prompt) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            
            // Construire le corps de la requête
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo");
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
                        System.out.println("✅ [INFO] Réponse OpenAI reçue avec succès");
                        return content;
                    }
                }
                
                // Erreur API
                if (rootNode.has("error")) {
                    String errorMessage = rootNode.get("error").get("message").asText();
                    return "Erreur OpenAI: " + errorMessage;
                }
                
                return "Réponse inattendue de OpenAI: " + responseBody;
            }
        } catch (Exception e) {
            return "Erreur de communication OpenAI: " + e.getMessage();
        }
    }
    
    /**
     * Met à jour la clé API OpenAI
     */
    public void updateApiKey(String newApiKey) {
        if (newApiKey != null && !newApiKey.trim().isEmpty()) {
            this.apiKey = newApiKey.trim();
            System.out.println("✅ [CONFIG] Clé API OpenAI mise à jour");
        }
    }
    
    /**
     * Vérifie si OpenAI est configuré
     */
    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }
}
