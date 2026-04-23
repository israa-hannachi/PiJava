package tn.esprit.services.cours;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.InputStream;
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

    private static final String API_KEY = "AIzaSyA0d-Ui9XjSruuwGeKRnexj_fA1vOindwk";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

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
    public String generateSummary(String htmlContent, String pdfPathOrUrl) {
        StringBuilder combinedContent = new StringBuilder();

        // 1. Ajouter le contenu texte (HTML)
        if (htmlContent != null && !htmlContent.trim().isEmpty()) {
            String cleanText = htmlContent.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            combinedContent.append("CONTENU TEXTUEL DU COURS :\n").append(cleanText).append("\n\n");
        }

        // 2. Extraire et ajouter le contenu du PDF
        if (pdfPathOrUrl != null && !pdfPathOrUrl.trim().isEmpty()) {
            String pdfText = extractTextFromPdf(pdfPathOrUrl);
            if (pdfText != null && !pdfText.isEmpty()) {
                combinedContent.append("CONTENU DU FICHIER PDF ASSOCIÉ :\n").append(pdfText).append("\n\n");
            }
        }

        if (combinedContent.length() == 0) {
            return "Aucun contenu (texte ou PDF) trouvé pour générer un résumé.";
        }

        String prompt = "Tu es un assistant pédagogique expert. " +
                "Voici les différentes sources de contenu d'un cours (texte et/ou PDF). " +
                "Peux-tu en faire un résumé GLOBAL, structuré et cohérent ? " +
                "Synthétise les informations importantes de toutes les sources fournies.\n\n" +
                combinedContent.toString();

        return callGeminiApi(prompt);
    }

    /**
     * Extrait le texte d'un PDF, qu'il soit local ou distant (Cloudinary).
     */
    private String extractTextFromPdf(String pathOrUrl) {
        try {
            if (pathOrUrl.startsWith("http")) {
                // Cas URL (Cloudinary)
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet httpGet = new HttpGet(pathOrUrl);
                    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                        try (InputStream is = response.getEntity().getContent();
                             PDDocument document = PDDocument.load(is)) {
                            return new PDFTextStripper().getText(document);
                        }
                    }
                }
            } else {
                // Cas Fichier Local
                File file = new File(pathOrUrl);
                if (file.exists()) {
                    try (PDDocument document = PDDocument.load(file)) {
                        return new PDFTextStripper().getText(document);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction PDF (" + pathOrUrl + ") : " + e.getMessage());
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
                    return "Erreur API Gemini (URL: " + API_URL + ") : " + rootNode.get("error").get("message").asText();
                }

                return "Réponse inattendue de l'IA (URL: " + API_URL + ") : " + responseBody;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la communication avec l'IA : " + e.getMessage();
        }
    }
}
