package tn.esprit.tools;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public final class TwoFactorAuthUtils {
    private static final Base32 BASE32 = new Base32();
    private static final int SECRET_SIZE_BYTES = 20;
    private static final int TIME_STEP_SECONDS = 30;

    private TwoFactorAuthUtils() {}

    /** Generates a random 32-character secret key for the user. */
    public static String generateSecret() {
        byte[] random = new byte[SECRET_SIZE_BYTES];
        new SecureRandom().nextBytes(random);
        return base32Encode(random);
    }

    /** Creates the special URL used to generate the QR Code for Google Authenticator. */
    public static String buildOtpAuthUri(String issuer, String accountName, String secret) {
        String safeIssuer = urlEncode(issuer);
        String safeAccount = urlEncode(accountName);
        return "otpauth://totp/" + safeIssuer + ":" + safeAccount
                + "?secret=" + secret
                + "&issuer=" + safeIssuer
                + "&algorithm=SHA1&digits=6&period=" + TIME_STEP_SECONDS;
    }

    /** Verifies the 6-digit code entered by the user against the current time and secret key. */
    public static boolean verifyCode(String secret, String code) {
        if (secret == null || secret.isBlank() || code == null) {
            return false;
        }

        String normalizedCode = code.replaceAll("\\s+", "");
        if (!normalizedCode.matches("\\d{6}")) {
            return false;
        }

        long timeWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        // Accept small clock drift (previous/current/next 2 windows => +/-60s).
        for (int i = -2; i <= 2; i++) {
            String expected = generateTotp(secret, timeWindow + i);
            if (normalizedCode.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static String generateTotp(String secret, long timeWindow) {
        try {
            byte[] key = base32Decode(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to generate TOTP", e);
        }
    }

    private static String base32Encode(byte[] data) {
        return BASE32.encodeToString(data).replace("=", "");
    }

    private static byte[] base32Decode(String encoded) {
        String clean = encoded.trim().replace("=", "").replaceAll("\\s+", "").toUpperCase();
        return BASE32.decode(clean);
    }

    private static String urlEncode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}
