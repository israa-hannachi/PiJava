package tn.esprit.services.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HuggingFaceService {

    // Token chargé depuis la variable d'environnement HF_TOKEN ou le fichier .env
    private static final String HF_TOKEN = System.getenv("HF_TOKEN") != null
            ? System.getenv("HF_TOKEN") : "";

    private static final String API_URL =
            "https://router.huggingface.co/hf-inference/models/" +
                    "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2" +
                    "/pipeline/sentence-similarity";

    /**
     * Compare la reponse de l etudiant avec la reponse correcte.
     * Retourne un score entre 0.0 et 1.0
     * Meme logique que compareWithNLP() en PHP/Python
     */
    public float compareWithNLP(String studentAnswer, String correctAnswer) {
        try {
            // Format correct pour sentence-similarity HuggingFace
            String body = "{"
                    + "\"inputs\": {"
                    + "\"source_sentence\": \"" + escapeJson(correctAnswer) + "\","
                    + "\"sentences\": [\"" + escapeJson(studentAnswer) + "\"]"
                    + "}"
                    + "}";

            System.out.println("HF envoi: " + body);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("HF response code: " + responseCode);

            if (responseCode == 200) {
                String response = new String(
                        conn.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );
                System.out.println("HF reponse: " + response);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response);

                // Retourne le premier score du tableau
                if (node.isArray() && node.size() > 0) {
                    float score = (float) node.get(0).asDouble();
                    System.out.println("HF similarite: " + score);
                    return score;
                }
            } else if (responseCode == 503) {
                System.out.println("HF: Modele en chargement, attente 5s...");
                Thread.sleep(5000);
                return compareWithNLP(studentAnswer, correctAnswer);
            } else {
                // Lire le message d erreur
                String error = new String(
                        conn.getErrorStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );
                System.err.println("HF erreur HTTP: "
                        + responseCode + " — " + error);
            }

        } catch (Exception e) {
            System.err.println("HuggingFace erreur: " + e.getMessage());
        }
        return 0.0f;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}