package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service IA — utilise Groq API (LLaMA 3) en priorité,
 * avec HuggingFace Router comme fallback secondaire.
 * Clé Groq gratuite sur : https://console.groq.com
 */
public class HuggingFaceService {

    private static volatile HuggingFaceService instance;

    // ── Groq API (primaire) ────────────────────────────────────────────────────
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL   = "llama-3.1-8b-instant";

    // ── HuggingFace Router (fallback) ──────────────────────────────────────────
    private static final String[] HF_API_URLS = {
            "https://router.huggingface.co/hf-inference/models/google/flan-t5-large",
            "https://router.huggingface.co/hf-inference/models/facebook/blenderbot-400M-distill"
    };

    private String groqApiKey;
    private String hfApiKey;
    private final HttpClient httpClient;

    private final List<String> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 10;
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();
    private volatile long lastRequestTime = 0;
    private static final long MIN_DELAY_MS = 0;

    // ─────────────────────────────────────────────────────────────────────────
    private HuggingFaceService() {
        this.groqApiKey = resolveKey("GROQ_API_KEY");
        this.hfApiKey   = resolveKey("HUGGINGFACE_API_KEY");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        if (groqApiKey != null && !groqApiKey.isBlank()) {
            System.out.println("✅ Service IA prêt — Groq API (LLaMA 3).");
        } else if (hfApiKey != null && !hfApiKey.isBlank()) {
            System.out.println("⚠️ Groq non configuré — HuggingFace activé.");
        } else {
            System.out.println("❌ Aucune clé API trouvée. Ajoutez GROQ_API_KEY dans .env");
        }
    }

    public static HuggingFaceService getInstance() {
        if (instance == null) {
            synchronized (HuggingFaceService.class) {
                if (instance == null) instance = new HuggingFaceService();
            }
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    public String sendMessage(String userMessage, String targetLanguage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Je n'ai pas reçu de message. Comment puis-je vous aider ?";
        }

        String cacheKey = userMessage.trim().toLowerCase() + "_" + targetLanguage;
        // if (responseCache.containsKey(cacheKey)) {
        //     return responseCache.get(cacheKey);
        // }

        // 1. Groq (LLaMA 3) — meilleure qualité
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            try {
                waitIfNeeded();
                String reply = callGroqAPI(userMessage, targetLanguage);
                if (reply != null && !reply.isBlank()) {
                    addToHistory("user", userMessage);
                    addToHistory("assistant", reply);
                    responseCache.put(cacheKey, reply);
                    return reply;
                }
            } catch (Exception e) {
                System.err.println("❌ Groq API échoué: " + e.getMessage());
            }
        }

        // 2. HuggingFace Router (fallback)
        if (hfApiKey != null && !hfApiKey.isBlank()) {
            try {
                waitIfNeeded();
                String reply = callHuggingFaceAPI(userMessage);
                if (reply != null && !reply.isBlank()) {
                    responseCache.put(cacheKey, reply);
                    return reply;
                }
            } catch (Exception e) {
                System.err.println("❌ HuggingFace API échoué: " + e.getMessage());
            }
        }

        // 3. Message d'erreur explicite (aucune clé configurée)
        if ((groqApiKey == null || groqApiKey.isBlank()) && (hfApiKey == null || hfApiKey.isBlank())) {
            return "⚠️ Aucune clé API configurée. Ajoutez GROQ_API_KEY=gsk_... dans le fichier .env et redémarrez l'application. Clé gratuite sur https://console.groq.com";
        }

        return "Le service IA est temporairement indisponible. Veuillez réessayer dans quelques instants.";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Groq API — format OpenAI, modèle LLaMA 3 8B
    // ─────────────────────────────────────────────────────────────────────────
    private String callGroqAPI(String userMessage, String targetLanguage) throws Exception {
        // Construit l'historique de conversation
        JSONArray messages = new JSONArray();
        
        String systemPrompt = "You are a professional AI assistant. STRICT RULE: You MUST answer EXACTLY in the SAME language as the user's input. If the user writes in English, you MUST answer ONLY in English. If the user writes in French, you MUST answer ONLY in French. Never mix languages. Do not apologize or explain this rule. Always provide a highly detailed, thorough, and comprehensive answer to the user's question. Act like an expert and write long, well-structured responses using paragraphs and bullet points.";
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));

        // Ajoute l'historique (contexte de conversation)
        for (Map.Entry<String, String> entry : getHistoryAsMessages()) {
            messages.put(new JSONObject()
                    .put("role", entry.getKey())
                    .put("content", entry.getValue()));
        }

        // Message actuel avec rappel strict
        String enrichedUserMessage = userMessage + "\n\n[SYSTEM INSTRUCTION: You MUST answer EXACTLY in the language of the question above. If the question is in English, reply ONLY in English. If it is in French, reply ONLY in French. No apologies, no explanations, no bilingual text. IMPORTANT: Generate a very LONG, DETAILED, and STRUCTURED response (like ChatGPT) with multiple paragraphs and bullet points.]";
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", enrichedUserMessage));

        JSONObject body = new JSONObject();
        body.put("model", GROQ_MODEL);
        body.put("messages", messages);
        body.put("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("📡 Groq API status: " + response.statusCode());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            JSONArray choices = json.optJSONArray("choices");
            if (choices != null && choices.length() > 0) {
                return choices.getJSONObject(0)
                        .getJSONObject("message")
                        .optString("content", "")
                        .trim();
            }
        } else if (response.statusCode() == 401) {
            System.err.println("❌ Clé Groq invalide. Vérifiez GROQ_API_KEY dans .env");
        } else {
            System.err.println("❌ Groq erreur " + response.statusCode() + ": " + response.body());
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HuggingFace Router — fallback secondaire
    // ─────────────────────────────────────────────────────────────────────────
    private String callHuggingFaceAPI(String userMessage) throws Exception {
        String prompt = "Question: " + userMessage + "\n\n[SYSTEM INSTRUCTION: Answer EXACTLY in the language of the question above. No mixing languages. Give a LONG and VERY DETAILED explanation with paragraphs and bullet points.]";

        JSONObject body = new JSONObject();
        body.put("inputs", prompt);
        body.put("parameters", new JSONObject()
                .put("max_new_tokens", 1024)
                .put("temperature", 0.7));
        body.put("wait_for_model", true);

        for (String apiUrl : HF_API_URLS) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + hfApiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(40))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📡 HF API (" + apiUrl.substring(apiUrl.lastIndexOf('/') + 1) + "): " + response.statusCode());

            if (response.statusCode() == 200) {
                String text = extractHFText(response.body());
                if (text != null && !text.isBlank()) return text;
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                System.err.println("❌ Clé HuggingFace invalide.");
                return null;
            } else if (response.statusCode() == 503) {
                Thread.sleep(5000);
            }
        }
        return null;
    }

    private String extractHFText(String body) {
        try {
            if (body == null || body.isBlank()) return null;
            if (body.trim().startsWith("[")) {
                JSONArray arr = new JSONArray(body);
                if (arr.length() > 0)
                    return arr.getJSONObject(0).optString("generated_text", "").trim();
            } else {
                return new JSONObject(body).optString("generated_text", "").trim();
            }
        } catch (Exception e) {
            System.err.println("❌ Parsing HF: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Historique de conversation
    // ─────────────────────────────────────────────────────────────────────────
    private void addToHistory(String role, String content) {
        conversationHistory.add(role + "::" + content);
        if (conversationHistory.size() > MAX_HISTORY * 2) {
            conversationHistory.remove(0);
            conversationHistory.remove(0);
        }
    }

    private List<Map.Entry<String, String>> getHistoryAsMessages() {
        List<Map.Entry<String, String>> result = new ArrayList<>();
        for (String entry : conversationHistory) {
            int idx = entry.indexOf("::");
            if (idx > 0) {
                result.add(new AbstractMap.SimpleEntry<>(
                        entry.substring(0, idx),
                        entry.substring(idx + 2)));
            }
        }
        return result;
    }

    private synchronized void waitIfNeeded() {
        long elapsed = System.currentTimeMillis() - lastRequestTime;
        if (elapsed < MIN_DELAY_MS) {
            try { Thread.sleep(MIN_DELAY_MS - elapsed); } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Résolution des clés API depuis env var ou .env
    // ─────────────────────────────────────────────────────────────────────────
    private String resolveKey(String keyName) {
        String val = System.getenv(keyName);
        if (val != null && !val.isBlank()) return val.trim();
        
        val = readKeyFromDotEnv(keyName);
        if (val != null && !val.isBlank()) return val;
        
        return readKeyFromConfigProperties(keyName);
    }

    private String readKeyFromConfigProperties(String keyName) {
        try {
            Properties config = new Properties();
            // 1. Essayer via le ClassLoader (plus robuste en production)
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");
            
            // 2. Essayer via chemin relatif si ClassLoader échoue
            if (is == null) {
                File file = new File("src/main/resources/config.properties");
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }
            
            if (is != null) {
                config.load(is);
                is.close();
                String val = config.getProperty(keyName);
                if (val != null && !val.isBlank()) {
                    System.out.println("✅ " + keyName + " trouvée dans: config.properties");
                    return val.trim();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lecture config.properties: " + e.getMessage());
        }
        return null;
    }

    private String readKeyFromDotEnv(String keyName) {
        List<Path> candidates = new ArrayList<>();
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (dir != null) {
            candidates.add(dir.resolve(".env"));
            dir = dir.getParent();
        }
        for (String extra : new String[]{"projetjava/.env", "PiJava-gestion-evenement/projetjava/.env"}) {
            candidates.add(Path.of(extra).toAbsolutePath());
        }

        java.nio.charset.Charset[] charsets = {
            java.nio.charset.StandardCharsets.UTF_8,
            java.nio.charset.Charset.forName("windows-1252")
        };

        for (Path envPath : candidates) {
            if (!Files.exists(envPath)) continue;
            for (java.nio.charset.Charset cs : charsets) {
                try {
                    for (String line : Files.readAllLines(envPath, cs)) {
                        String t = line.replace("\uFEFF", "").replace("\r", "").trim();
                        if (t.startsWith(keyName + "=")) {
                            String v = t.substring(keyName.length() + 1).trim();
                            int ci = v.indexOf('#');
                            if (ci > 0) v = v.substring(0, ci).trim();
                            if (!v.isBlank()) {
                                System.out.println("✅ " + keyName + " trouvée dans: " + envPath);
                                return v;
                            }
                        }
                    }
                    break;
                } catch (IOException ignored) {}
            }
        }
        return null;
    }
    
    public String transcribeAudio(java.io.File audioFile, String language) throws Exception {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String GROQ_AUDIO_API_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
        
        // Map language name to ISO code for Whisper dynamically
        String langCode = "fr"; // Default
        if (language != null) {
            String targetLang = language.toLowerCase();
            for (String iso : java.util.Locale.getISOLanguages()) {
                String display = new java.util.Locale(iso).getDisplayLanguage(java.util.Locale.FRENCH).toLowerCase();
                if (display.equals(targetLang)) {
                    langCode = iso;
                    break;
                }
            }
        }

        byte[] fileBytes = java.nio.file.Files.readAllBytes(audioFile.toPath());

        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream out = new java.io.DataOutputStream(byteStream);

        // Add file
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + audioFile.getName() + "\"\r\n");
        out.writeBytes("Content-Type: audio/wav\r\n\r\n");
        out.write(fileBytes);
        out.writeBytes("\r\n");

        // Add model
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
        out.writeBytes("whisper-large-v3\r\n");

        // Add language
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
        out.writeBytes(langCode + "\r\n");

        // End boundary
        out.writeBytes("--" + boundary + "--\r\n");
        out.flush();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_AUDIO_API_URL))
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofByteArray(byteStream.toByteArray()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("🎤 Groq Whisper API status: " + response.statusCode());
        System.out.println("Response: " + response.body());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.optString("text", "");
        } else {
            throw new Exception("Erreur API Whisper: " + response.statusCode() + " - " + response.body());
        }
    }
}