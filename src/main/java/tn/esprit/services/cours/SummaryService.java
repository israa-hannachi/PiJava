package tn.esprit.services.cours;

import java.util.*;

/**
 * Service unifié pour la génération de résumés avec fallback intelligent entre providers
 */
public class SummaryService {
    
    private static SummaryService instance;
    private final Properties config;
    private final List<String> providersPriority;
    
    private SummaryService() {
        this.config = new Properties();
        loadConfiguration();
        this.providersPriority = loadProvidersPriority();
    }
    
    public static synchronized SummaryService getInstance() {
        if (instance == null) {
            instance = new SummaryService();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis le fichier config.properties
     */
    private void loadConfiguration() {
        try {
            java.io.InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (configStream == null) {
                configStream = new java.io.FileInputStream("src/main/resources/config.properties");
            }
            
            if (configStream != null) {
                config.load(configStream);
                configStream.close();
            }
        } catch (Exception e) {
            System.err.println("❌ [CONFIG] Erreur chargement configuration: " + e.getMessage());
        }
    }
    
    /**
     * Charge l'ordre de priorité des providers IA
     */
    private List<String> loadProvidersPriority() {
        String priority = config.getProperty("AI_PROVIDERS_PRIORITY", "gemini,openai");
        return Arrays.asList(priority.split(","));
    }
    
    /**
     * Génère un résumé en utilisant le premier provider disponible
     */
    public String generateSummary(String htmlContent, String pdfUrl) {
        List<String> errors = new ArrayList<>();
        
        for (String provider : providersPriority) {
            if (!isProviderEnabled(provider)) {
                continue;
            }
            
            try {
                System.out.println("🤖 [INFO] Tentative avec le provider: " + provider);
                
                String result = switch (provider.toLowerCase()) {
                    case "gemini" -> tryGemini(htmlContent, pdfUrl);
                    case "openai" -> tryOpenAI(htmlContent, pdfUrl);
                    case "huggingface" -> tryHuggingFace(htmlContent, pdfUrl);
                    case "groq" -> tryGroq(htmlContent, pdfUrl);
                    default -> {
                        errors.add(provider + " : provider non reconnu");
                        yield null;
                    }
                };
                
                if (result != null && !result.contains("Erreur") && !result.contains("⚠️")) {
                    System.out.println("✅ [INFO] Résumé généré avec succès par " + provider);
                    return result;
                } else if (result != null) {
                    errors.add(provider + " : " + result);
                }
                
            } catch (Exception e) {
                errors.add(provider + " : " + e.getMessage());
            }
        }
        
        // Tous les providers ont échoué
        return generateErrorSummary(errors);
    }
    
    /**
     * Essaie de générer un résumé avec Gemini
     */
    private String tryGemini(String htmlContent, String pdfUrl) {
        boolean geminiEnabled = Boolean.parseBoolean(config.getProperty("ENABLE_GEMINI", "true"));
        if (!geminiEnabled) {
            return "Gemini désactivé dans la configuration";
        }
        
        GeminiService geminiService = GeminiService.getInstance();
        if (!geminiService.isApiKeyValid()) {
            return "Clé API Gemini invalide";
        }
        
        return geminiService.generateSummary(htmlContent, pdfUrl);
    }
    
    /**
     * Essaie de générer un résumé avec OpenAI
     */
    private String tryOpenAI(String htmlContent, String pdfUrl) {
        boolean openaiEnabled = Boolean.parseBoolean(config.getProperty("ENABLE_OPENAI", "true"));
        if (!openaiEnabled) {
            return "OpenAI désactivé dans la configuration";
        }
        
        OpenAIService openaiService = OpenAIService.getInstance();
        if (!openaiService.isConfigured()) {
            return "Clé API OpenAI non configurée";
        }
        
        return openaiService.generateSummary(htmlContent, pdfUrl);
    }
    
    /**
     * Essaie de générer un résumé avec Hugging Face
     */
    private String tryHuggingFace(String htmlContent, String pdfUrl) {
        boolean huggingfaceEnabled = Boolean.parseBoolean(config.getProperty("ENABLE_HUGGINGFACE", "true"));
        if (!huggingfaceEnabled) {
            return "Hugging Face désactivé dans la configuration";
        }
        
        HuggingFaceService huggingfaceService = HuggingFaceService.getInstance();
        // Hugging Face fonctionne même sans clé API
        
        return huggingfaceService.generateSummary(htmlContent, pdfUrl);
    }
    
    /**
     * Essaie de générer un résumé avec Groq
     */
    private String tryGroq(String htmlContent, String pdfUrl) {
        boolean groqEnabled = Boolean.parseBoolean(config.getProperty("ENABLE_GROQ", "true"));
        if (!groqEnabled) {
            return "Groq désactivé dans la configuration";
        }
        
        GroqService groqService = GroqService.getInstance();
        if (!groqService.isConfigured()) {
            return "Clé API Groq non configurée";
        }
        
        return groqService.generateSummary(htmlContent, pdfUrl);
    }
    
    /**
     * Vérifie si un provider est activé
     */
    private boolean isProviderEnabled(String provider) {
        return switch (provider.toLowerCase()) {
            case "gemini" -> Boolean.parseBoolean(config.getProperty("ENABLE_GEMINI", "true"));
            case "openai" -> Boolean.parseBoolean(config.getProperty("ENABLE_OPENAI", "true"));
            case "huggingface" -> Boolean.parseBoolean(config.getProperty("ENABLE_HUGGINGFACE", "true"));
            case "groq" -> Boolean.parseBoolean(config.getProperty("ENABLE_GROQ", "true"));
            default -> false;
        };
    }
    
    /**
     * Génère un message d'erreur détaillé
     */
    private String generateErrorSummary(List<String> errors) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("⚠️ Tous les providers IA ont échoué.\n\n");
        errorMsg.append("Solutions possibles :\n");
        errorMsg.append("• Attendez quelques minutes et réessayez (pour les quotas temporaires)\n");
        errorMsg.append("• Configurez une clé API OpenAI dans config.properties\n");
        errorMsg.append("• Obtenez une nouvelle clé Gemini sur https://aistudio.google.com/apikey\n");
        errorMsg.append("• Obtenez une clé OpenAI sur https://platform.openai.com/api-keys\n\n");
        errorMsg.append("Détails des erreurs :\n");
        for (String err : errors) {
            errorMsg.append("  - ").append(err).append("\n");
        }
        return errorMsg.toString();
    }
    
    /**
     * Met à jour l'ordre de priorité des providers
     */
    public void updateProvidersPriority(String newPriority) {
        if (newPriority != null && !newPriority.trim().isEmpty()) {
            config.setProperty("AI_PROVIDERS_PRIORITY", newPriority);
            providersPriority.clear();
            providersPriority.addAll(Arrays.asList(newPriority.split(",")));
            System.out.println("✅ [CONFIG] Ordre des providers mis à jour: " + newPriority);
        }
    }
    
    /**
     * Retourne la liste des providers disponibles et leur statut
     */
    public Map<String, Boolean> getProvidersStatus() {
        Map<String, Boolean> status = new HashMap<>();
        
        for (String provider : providersPriority) {
            boolean available = switch (provider.toLowerCase()) {
                case "gemini" -> {
                    GeminiService gemini = GeminiService.getInstance();
                    yield isProviderEnabled(provider) && gemini.isApiKeyValid();
                }
                case "openai" -> {
                    OpenAIService openai = OpenAIService.getInstance();
                    yield isProviderEnabled(provider) && openai.isConfigured();
                }
                case "huggingface" -> {
                    HuggingFaceService huggingface = HuggingFaceService.getInstance();
                    yield isProviderEnabled(provider) && huggingface.isConfigured();
                }
                case "groq" -> {
                    GroqService groq = GroqService.getInstance();
                    yield isProviderEnabled(provider) && groq.isConfigured();
                }
                default -> false;
            };
            status.put(provider, available);
        }
        
        return status;
    }
}
