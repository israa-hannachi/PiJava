package tn.esprit.services.event;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class EventContentGenerationService {
    private static final String RAPID_API_KEY = "026cfd2a73mshe1e4690e16b5752p197f8cjsn962d10d6281b";
    private static final String RAPID_API_HOST = "ai-content-writer.p.rapidapi.com";

    public String generateContent(String topic) {
        if (topic == null || topic.isBlank()) {
            return null;
        }

        String apiContent = getApiContent(topic);
        if (apiContent != null && !apiContent.isBlank()) {
            return apiContent.trim();
        }

        GeminiAiService fallbackService = new GeminiAiService();
        return fallbackService.generateRecommendation(
                "Redige un court avis naturel en francais a propos de ce sujet evenementiel: "
                        + topic
                        + ". Retourne uniquement le texte final, sans introduction."
        );
    }

    private String getApiContent(String topic) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ai-content-writer.p.rapidapi.com/data"))
                    .header("x-rapidapi-key", RAPID_API_KEY)
                    .header("x-rapidapi-host", RAPID_API_HOST)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("topic=" + URLEncoder.encode(topic, StandardCharsets.UTF_8)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                // Extract "content" field from JSON manually to avoid dependencies
                String extracted = extractJsonValue(body, "content");
                return extracted != null ? extracted : body;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) return null;

        int valueStart = json.indexOf('"', keyIndex + marker.length());
        if (valueStart < 0) return null;

        int valueEnd = json.indexOf('"', valueStart + 1);
        while (valueEnd > 0 && json.charAt(valueEnd - 1) == '\\') {
            valueEnd = json.indexOf('"', valueEnd + 1);
        }
        if (valueEnd < 0) return null;

        String result = json.substring(valueStart + 1, valueEnd);
        // Basic unescape for JSON
        return result.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "");
    }
}
