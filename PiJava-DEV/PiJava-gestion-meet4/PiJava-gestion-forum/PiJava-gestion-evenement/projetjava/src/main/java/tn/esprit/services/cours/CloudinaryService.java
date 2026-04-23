package tn.esprit.services.cours;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Service d'upload de fichiers (PDF) vers Cloudinary.
 * <p>
 * Configuration :  dbkehfwxu / 979582436561551 / 9_8ycl9vKNFQYwpGBV3OhSIp8jg
 *                  (mêmes credentials que le projet web)
 */
public class CloudinaryService {

    // ──────────────────────────────────────────────────────────────────────────────
    //  Credentials Cloudinary  — identiques à ceux du projet Symfony
    // ──────────────────────────────────────────────────────────────────────────────
    private static final String CLOUD_NAME = "dbkehfwxu";        // ex: d3bkehfwxu
    private static final String API_KEY    = "979582436561551";    // à remplir
    private static final String API_SECRET = "9_8ycl9vKNFQYwpGBV3OhSIp8jg"; // à remplir

    private static final String UPLOAD_FOLDER = "gestion-cours/cours";

    private final Cloudinary cloudinary;
    private static CloudinaryService instance;

    private CloudinaryService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key",    API_KEY,
                "api_secret", API_SECRET,
                "secure",     true
        ));
    }

    public static CloudinaryService getInstance() {
        if (instance == null) instance = new CloudinaryService();
        return instance;
    }

    /**
     * Upload un fichier PDF vers Cloudinary.
     *
     * @param file fichier local
     * @return URL sécurisée (CLOUDINARY_URL=cloudinary://979582436561551:9_8ycl9vKNFQYwpGBV3OhSIp8jg@dbkehfwxu)  ou null en cas d'erreur
     */
    @SuppressWarnings("unchecked")
    public String uploadPdf(File file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder",          UPLOAD_FOLDER,
                    "resource_type",   "raw",          // obligatoire pour PDF
                    "use_filename",    true,
                    "unique_filename", true
            );
            Map<String, Object> result = cloudinary.uploader().upload(file, options);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            System.err.println("❌ Erreur upload Cloudinary : " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime un fichier Cloudinary à partir de son URL sécurisée.
     * Utilisé lors de la suppression/modification d'un cours.
     *
     * @param secureUrl URL renvoyée lors de l'upload
     */
    public void deletePdf(String secureUrl) {
        if (secureUrl == null || !secureUrl.startsWith("https://res.cloudinary.com")) return;
        try {
            // Extraire le public_id depuis l'URL
            // Exemple URL : https://res.cloudinary.com/dbkehfwxu/raw/upload/v17.../gestion-cours/cours/nom.pdf
            String[] parts = secureUrl.split("/upload/");
            if (parts.length < 2) return;
            String afterUpload = parts[1];
            // Supprimer le versioning "v12345678/"
            String publicId = afterUpload.replaceFirst("^v\\d+/", "");
            // Supprimer l'extension
            int dotIdx = publicId.lastIndexOf('.');
            if (dotIdx > 0) publicId = publicId.substring(0, dotIdx);

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException e) {
            System.err.println("⚠️ Erreur suppression Cloudinary : " + e.getMessage());
        }
    }

    /**
     * Renvoie vrai si une URL est hébergée sur Cloudinary.
     */
    public static boolean isCloudinaryUrl(String url) {
        return url != null && url.startsWith("https://res.cloudinary.com");
    }
}