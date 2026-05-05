package tn.esprit.tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FaceAuthAssets {

    private static final String[] MODEL_FILES = new String[] {
            "face_landmark_68_model-shard1",
            "face_landmark_68_model-weights_manifest.json",
            "face_recognition_model-shard1",
            "face_recognition_model-shard2",
            "face_recognition_model-weights_manifest.json",
            "ssd_mobilenetv1_model-shard1",
            "ssd_mobilenetv1_model-shard2",
            "ssd_mobilenetv1_model-weights_manifest.json"
    };

    private FaceAuthAssets() {
    }

    public static Path prepareAssetsDirectory() throws IOException {
        Path targetDir = Path.of("face-auth-assets");
        Path modelsDir = targetDir.resolve("models");
        System.out.println("📂 Creating assets directories: " + targetDir.toAbsolutePath());
        Files.createDirectories(modelsDir);

        System.out.println("📂 Preparing Face ID assets in: " + targetDir);
        copyResource("/tn/esprit/face/face_auth.html", targetDir.resolve("face_auth.html"));

        for (String modelFile : MODEL_FILES) {
            copyResource("/assets/face-models/" + modelFile, modelsDir.resolve(modelFile));
        }

        System.out.println("✅ Face ID assets prepared successfully.");
        return targetDir;
    }

    private static void copyResource(String resourcePath, Path target) throws IOException {
        if (Files.exists(target)) {
            // If the file already exists, we skip copying to avoid "file in use" errors from Windows
            return;
        }
        try (InputStream inputStream = FaceAuthAssets.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Missing face-auth resource: " + resourcePath);
            }
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
