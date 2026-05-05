package tn.esprit.services.event;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KonnectService {
    private static final String API_URL = "https://api.konnect.network/api/v2/payments/init-payment";
    private static final String API_KEY = "67bc94e1d13f56a36f56199a:0Sj7tA8f7U2H1v9mvgqIw0tj6eNUgdO"; // Mock key

    public String initPayment(double amount, String firstName, String lastName, String email) {
        try {
            String json = "{"
                    + "\"receiverWalletId\": \"67bc94e1d13f56a36f56199d\","
                    + "\"amount\": " + (amount * 1000) + ","
                    + "\"token\": \"TND\","
                    + "\"firstName\": \"" + firstName + "\","
                    + "\"lastName\": \"" + lastName + "\","
                    + "\"email\": \"" + email + "\","
                    + "\"type\": \"immediate\","
                    + "\"description\": \"Paiement reservation Naja7ni\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("x-api-key", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Return the full response for "visibility" as requested by user
            return "Statut: " + response.statusCode() + "\nReponse API:\n" + response.body();
        } catch (Exception e) {
            return "Erreur API Konnect: " + e.getMessage();
        }
    }
}
