import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class TestGroq {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("GROQ_API_KEY"); // Charger depuis variable d'environnement
        String body = "{\"model\":\"llama3-8b-8192\",\"messages\":[{\"role\":\"user\",\"content\":\"Dis bonjour en une phrase.\"}],\"max_tokens\":50}";

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + res.statusCode());
        System.out.println("Body: " + res.body());
    }
}
