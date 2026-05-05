package tn.esprit.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class FaceAuthUtils {

    public static final double DEFAULT_THRESHOLD = 0.6d;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FaceAuthUtils() {
    }

    public static double calculateDistance(String storedDescriptorJson, String providedDescriptorJson) {
        double[] stored = parseDescriptor(storedDescriptorJson);
        double[] provided = parseDescriptor(providedDescriptorJson);

        if (stored.length != provided.length) {
            return Double.MAX_VALUE;
        }

        double sum = 0.0d;
        for (int i = 0; i < stored.length; i++) {
            double diff = stored[i] - provided[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    public static double[] parseDescriptor(String descriptorJson) {
        if (descriptorJson == null || descriptorJson.isBlank()) {
            return new double[0];
        }

        try {
            JsonNode root = MAPPER.readTree(descriptorJson);
            JsonNode arrayNode = root;
            if (!root.isArray() && root.has("descriptor")) {
                arrayNode = root.get("descriptor");
            }
            if (arrayNode == null || !arrayNode.isArray()) {
                return new double[0];
            }

            double[] descriptor = new double[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                descriptor[i] = arrayNode.get(i).asDouble();
            }
            return descriptor;
        } catch (Exception e) {
            return new double[0];
        }
    }
}
