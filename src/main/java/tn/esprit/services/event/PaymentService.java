package tn.esprit.services.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PaymentService {
    private static final String STRIPE_API_URL = "https://api.stripe.com/v1/payment_intents";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String secretKey;
    private final String publishableKey;

    public PaymentService() {
        Properties props = loadStripeProperties();

        // 1. Prefer environment variables (for CI/production)
        String secret = System.getenv("STRIPE_SECRET_KEY");
        String pub    = System.getenv("STRIPE_PUBLISHABLE_KEY");

        // 2. Fall back to local stripe.properties (for local development)
        if (secret == null || secret.isBlank()) {
            secret = props.getProperty("stripe.secret.key", "");
        }
        if (pub == null || pub.isBlank()) {
            pub = props.getProperty("stripe.publishable.key", "");
        }

        this.secretKey      = secret;
        this.publishableKey = pub;
    }

    /** Returns the publishable key (safe to use in UI). */
    public String getPublishableKey() {
        return publishableKey;
    }

    private Properties loadStripeProperties() {
        Properties props = new Properties();

        // Try loading from classpath first (packaged JAR)
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("stripe.properties")) {
            if (in != null) { props.load(in); return props; }
        } catch (IOException ignored) {}

        // Try loading from project root (IDE / local dev)
        String[] candidates = {
            "stripe.properties",
            "projetjava/stripe.properties",
            "PiJava-gestion-jeux/projetjava/stripe.properties"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                    return props;
                } catch (IOException ignored) {}
            }
        }
        return props;
    }

    public String createPaymentIntent(double amount, String currency, String description) throws IOException, InterruptedException {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IOException("Stripe secret key not configured. Please set the STRIPE_SECRET_KEY environment variable.");
        }
        long amountInMinorUnit = Math.round(amount * 100);
        String formBody =
                "amount=" + amountInMinorUnit
                        + "&currency=" + encode(currency)
                        + "&description=" + encode(description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STRIPE_API_URL))
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Stripe API error " + response.statusCode() + ": " + response.body());
        }

        String clientSecret = extractJsonValue(response.body(), "client_secret");
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IOException("Stripe response did not include client_secret.");
        }
        return clientSecret;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) {
            return null;
        }

        int valueStart = json.indexOf('"', keyIndex + marker.length());
        if (valueStart < 0) {
            return null;
        }

        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd < 0) {
            return null;
        }

        return json.substring(valueStart + 1, valueEnd);
    }
}
