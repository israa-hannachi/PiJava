package tn.esprit.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public final class GoogleOAuthService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";
    private static final int LOCAL_PORT = Integer.parseInt(System.getProperty("naja7ni.google.redirectPort", "8765"));
    private static final String DEFAULT_CLIENT_ID = "DUMMY_CLIENT_ID";
    private static final String DEFAULT_CLIENT_SECRET = "DUMMY_CLIENT_SECRET";

    private GoogleOAuthService() {}

    public static GoogleUserInfo signIn() throws Exception {
        String clientId = requiredValue("GOOGLE_OAUTH_CLIENT_ID", "google.oauth.clientId", DEFAULT_CLIENT_ID);
        String clientSecret = optionalValue("GOOGLE_OAUTH_CLIENT_SECRET", "google.oauth.clientSecret", DEFAULT_CLIENT_SECRET);
        String redirectUri = System.getProperty("naja7ni.google.redirectUri", "http://localhost:" + LOCAL_PORT + "/oauth2callback");

        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = startCallbackServer(codeFuture);

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        String state = UUID.randomUUID().toString();
        String authUrl = AUTH_URL
                + "?client_id=" + url(clientId)
                + "&redirect_uri=" + url(redirectUri)
                + "&response_type=code"
                + "&scope=" + url("openid email profile")
                + "&access_type=offline"
                + "&prompt=consent"
            + "&code_challenge=" + url(codeChallenge)
            + "&code_challenge_method=S256"
                + "&state=" + url(state);

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create(authUrl));
        } else {
            throw new IllegalStateException("Desktop browsing is not supported on this system.");
        }

        String code;
        try {
            code = codeFuture.get();
        } finally {
            server.stop(0);
        }

        Map<String, String> tokenData = exchangeCodeForTokens(code, clientId, clientSecret, redirectUri, codeVerifier);
        String accessToken = tokenData.get("access_token");
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Google did not return an access token.");
        }

        HttpRequest userInfoReq = HttpRequest.newBuilder(URI.create(USERINFO_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> userInfoResp = HTTP.send(userInfoReq, HttpResponse.BodyHandlers.ofString());
        if (userInfoResp.statusCode() >= 300) {
            throw new IllegalStateException("Failed to read Google profile: HTTP " + userInfoResp.statusCode());
        }

        JsonNode root = MAPPER.readTree(userInfoResp.body());
        return new GoogleUserInfo(
                text(root, "email"),
                text(root, "given_name"),
                text(root, "family_name"),
                text(root, "picture"),
                text(root, "name")
        );
    }

    private static HttpServer startCallbackServer(CompletableFuture<String> codeFuture) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", LOCAL_PORT), 0);
        server.createContext("/oauth2callback", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            Map<String, String> params = parseQuery(query);
            String code = params.get("code");
            String html = "<html><body><h2>Google sign-in completed.</h2>You can return to the app.</body></html>";
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
            if (code != null && !code.isBlank()) {
                codeFuture.complete(code);
            } else {
                codeFuture.completeExceptionally(new IllegalStateException("No authorization code returned by Google."));
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        return server;
    }

    private static Map<String, String> exchangeCodeForTokens(String code, String clientId, String clientSecret, String redirectUri, String codeVerifier) throws Exception {
        String body = "code=" + url(code)
                + "&client_id=" + url(clientId)
                + "&redirect_uri=" + url(redirectUri)
                + "&grant_type=authorization_code"
                + "&code_verifier=" + url(codeVerifier);

        if (clientSecret != null && !clientSecret.isBlank()) {
            body += "&client_secret=" + url(clientSecret);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Token exchange failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        java.util.HashMap<String, String> tokens = new java.util.HashMap<>();
        tokens.put("access_token", text(root, "access_token"));
        tokens.put("id_token", text(root, "id_token"));
        tokens.put("refresh_token", text(root, "refresh_token"));
        return tokens;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return map;
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    private static String requiredValue(String envName, String propertyName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = System.getProperty(propertyName);
        }
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + envName + " (or system property: " + propertyName + ")");
        }
        return value.trim();
    }

    private static String optionalValue(String envName, String propertyName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = System.getProperty(propertyName);
        }
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }
        return value == null ? null : value.trim();
    }

    private static String optionalEnv(String name) {
        String value = System.getenv(name);
        return value == null ? null : value.trim();
    }

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static final class GoogleUserInfo {
        private final String email;
        private final String givenName;
        private final String familyName;
        private final String pictureUrl;
        private final String displayName;

        public GoogleUserInfo(String email, String givenName, String familyName, String pictureUrl, String displayName) {
            this.email = email;
            this.givenName = givenName;
            this.familyName = familyName;
            this.pictureUrl = pictureUrl;
            this.displayName = displayName;
        }

        public String getEmail() { return email; }
        public String getGivenName() { return givenName; }
        public String getFamilyName() { return familyName; }
        public String getPictureUrl() { return pictureUrl; }
        public String getDisplayName() { return displayName; }
    }
}
