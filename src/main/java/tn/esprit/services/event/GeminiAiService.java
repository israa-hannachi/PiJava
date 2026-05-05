package tn.esprit.services.event;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiAiService {
    private static final String API_KEY = "AIzaSyCYydFS3bZ4YGAfDGomsrAFWMcYf9bIGYo";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + API_KEY;

    public String generateRecommendation(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Sanitize prompt for raw JSON insertion
            String sanitizedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
            String jsonInputString = "{\"contents\": [{\"parts\":[{\"text\": \"" + sanitizedPrompt + "\"}]}]}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() != 200) {
                return "Erreur lors de la communication avec l'IA. Code HTTP: " + conn.getResponseCode();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String jsonResponse = response.toString();
            // Basic JSON extraction for the 'text' node
            int textIndex = jsonResponse.indexOf("\"text\": \"");
            if (textIndex != -1) {
                int start = textIndex + 9;
                // Find the next unescaped quote
                int end = start;
                while (end < jsonResponse.length()) {
                    if (jsonResponse.charAt(end) == '"' && jsonResponse.charAt(end - 1) != '\\') {
                        break;
                    }
                    end++;
                }
                if (end < jsonResponse.length()) {
                    return jsonResponse.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }

            return "L'IA n'a pas pu générer une recommandation exploitable.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur IA: " + e.getMessage();
        }
    }
}
