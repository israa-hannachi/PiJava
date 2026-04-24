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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service avancé pour interagir avec l'IA Google Gemini.
 * Capable de lire le texte HTML et d'extraire le contenu des fichiers PDF (locaux ou Cloudinary).
 */
public class GeminiService {

    private static GeminiService instance;
    private final ObjectMapper objectMapper;

    // Utilisation de la clé fournie par l'utilisateur
    private static final String API_KEY = "AIzaSyCQIwAdfnLMP_VROWtNhPUoumgMsfvI5Wo";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private GeminiService() {
        this.objectMapper = new ObjectMapper();
    }

    public static synchronized GeminiService getInstance() {
        if (instance == null) {
            instance = new GeminiService();
        }
        return instance;
    }

    /**
     * Génère un résumé en combinant le contenu HTML et le contenu du fichier PDF si présent.
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
            return "Aucun contenu (texte ou PDF) trouvé pour générer un résumé. Veuillez remplir le contenu du cours ou ajouter un fichier PDF.";
        }

        String fullPrompt = "Tu es un assistant pédagogique expert. " +
                "Voici les sources de contenu d'un cours. " +
                "Peux-tu en faire un résumé structuré, clair et pédagogique ?\n\n" +
                finalPromptContent;

        System.out.println("📤 [INFO] Envoi de la requête à Gemini (" + finalPromptContent.length() + " caractères)");
        return callGeminiApi(fullPrompt);
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

    private String callGeminiApi(String prompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);

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

                if (rootNode.has("candidates") && rootNode.get("candidates").isArray() && rootNode.get("candidates").size() > 0) {
                    JsonNode candidate = rootNode.get("candidates").get(0);
                    if (candidate.has("content") && candidate.get("content").has("parts")) {
                        return candidate.get("content").get("parts").get(0).get("text").asText();
                    }
                }

                if (rootNode.has("error")) {
                    return "Erreur API Gemini : " + rootNode.get("error").get("message").asText();
                }

                return "Réponse inattendue de l'IA : " + responseBody;
            }
        } catch (Exception e) {
            return "Erreur de communication avec l'IA : " + e.getMessage();
        }
    }
}
