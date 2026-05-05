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
 * Service gratuit et illimité utilisant Hugging Face Inference API
 * Modèle utilisé : Mistral-7B-Instruct-v0.2 (gratuit et performant)
 */
public class HuggingFaceService {
    
    private static HuggingFaceService instance;
    private final ObjectMapper objectMapper;
    private String apiKey;
    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2";
    
    private HuggingFaceService() {
        this.objectMapper = new ObjectMapper();
        loadConfiguration();
    }
    
    public static synchronized HuggingFaceService getInstance() {
        if (instance == null) {
            instance = new HuggingFaceService();
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
                this.apiKey = config.getProperty("HUGGINGFACE_API_KEY", "");
                
                if (!apiKey.isEmpty()) {
                    System.out.println("✅ [CONFIG] Clé API Hugging Face chargée");
                } else {
                    System.out.println("ℹ️ [CONFIG] Hugging Face utilisé sans clé (gratuit)");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [CONFIG] Erreur chargement Hugging Face: " + e.getMessage());
            this.apiKey = "";
        }
    }
    
    /**
     * Génère un résumé en utilisant Hugging Face Mistral-7B
     */
    public String generateSummary(String htmlContent, String pdfUrl) {
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
        
        // Prompt optimisé pour Mistral-7B
        String prompt = "Tu es un assistant pédagogique expert. Voici les sources de contenu d'un cours. Résume en français ce contenu de manière claire et structurée :\n\n" + finalPromptContent + "\n\nRésumé :";
        
        System.out.println("📤 [INFO] Envoi de la requête à Hugging Face Mistral-7B");
        return callHuggingFace(prompt);
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
     * Appelle l'API Hugging Face Inference
     */
    private String callHuggingFace(String prompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(MODEL_URL);
            
            // Ajouter la clé API si disponible (optionnel pour les modèles gratuits)
            if (!apiKey.isEmpty()) {
                httpPost.setHeader("Authorization", "Bearer " + apiKey);
            }
            httpPost.setHeader("Content-Type", "application/json");
            
            // Construire le corps de la requête pour Hugging Face
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_new_tokens", 500);
            parameters.put("temperature", 0.7);
            parameters.put("do_sample", true);
            parameters.put("return_full_text", false);
            requestBody.put("parameters", parameters);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(responseBody);
                
                // Hugging Face retourne un tableau de réponses
                if (rootNode.isArray() && rootNode.size() > 0) {
                    JsonNode firstResult = rootNode.get(0);
                    if (firstResult.has("generated_text")) {
                        String summary = firstResult.get("generated_text").asText();
                        
                        // Nettoyer la réponse
                        summary = summary.replace(prompt, "").trim();
                        if (summary.startsWith("Résumé :")) {
                            summary = summary.substring(9).trim();
                        }
                        
                        System.out.println("✅ [INFO] Résumé généré avec succès par Hugging Face");
                        return summary;
                    }
                }
                
                // Erreur API
                if (rootNode.has("error")) {
                    String errorMessage = rootNode.get("error").asText();
                    return "Erreur Hugging Face: " + errorMessage;
                }
                
                return "Réponse inattendue de Hugging Face: " + responseBody;
            }
        } catch (Exception e) {
            return "Erreur de communication Hugging Face: " + e.getMessage();
        }
    }
    
    /**
     * Met à jour la clé API Hugging Face (optionnel)
     */
    public void updateApiKey(String newApiKey) {
        if (newApiKey != null && !newApiKey.trim().isEmpty()) {
            this.apiKey = newApiKey.trim();
            System.out.println("✅ [CONFIG] Clé API Hugging Face mise à jour");
        }
    }
    
    /**
     * Vérifie si le service est configuré (fonctionne même sans clé)
     */
    public boolean isConfigured() {
        return true; // Hugging Face fonctionne même sans clé API
    }
    
    /**
     * Test la connexion au service
     */
    public String testConnection() {
        try {
            String testPrompt = "Test simple: Résume 'Le soleil brille'";
            String result = callHuggingFace(testPrompt);
            if (!result.contains("Erreur") && !result.contains("Erreur de communication")) {
                return "✅ Hugging Face fonctionne parfaitement";
            }
            return "❌ Erreur de connexion: " + result;
        } catch (Exception e) {
            return "❌ Erreur de test: " + e.getMessage();
        }
    }
}
