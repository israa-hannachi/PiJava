package tn.esprit.services.security;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tn.esprit.services.mail.SmtpConfig;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

/**
 * Service that handles password‑reset flow:
 *   1️⃣ generate a 6‑digit code
 *   2️⃣ store it temporarily (default 10 min)
 *   3️⃣ send the code by email using Gmail SMTP
 *   4️⃣ verify a submitted code
 */
public class PasswordResetService {
    private static final String FROM_EMAIL = "naja7ni.service@gmail.com";
    private static final String APP_PASSWORD = "mbgnpyplfphnpfpd"; // supplied by the user
    private static final long EXPIRY_MILLIS = 10 * 60 * 1000; // 10 minutes
    private static final Random RANDOM = new Random();

    private final Map<String, ResetEntry> pending = new ConcurrentHashMap<>();

    /** Holds a reset code and its expiration timestamp. */
    private static final class ResetEntry {
        final String code;
        final long expiresAt;
        ResetEntry(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    /** Generates a 6‑digit numeric code. */
    private static String generateCode() {
        int number = 100000 + RANDOM.nextInt(900000); // ensures 6 digits
        return String.valueOf(number);
    }

    /** Sends the reset code to the provided e‑mail address. */
    /** Sends the reset code to the provided e‑mail address using SMTP. */
    private static void sendEmail(String toEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Naja7ni – Password Reset Code");
        String html = "<p>Hello,</p>"
                + "<p>Your password‑reset verification code is: <strong>" + code + "</strong></p>"
                + "<p>This code will expire in 10 minutes. If you did not request a password reset, you can safely ignore this email.</p>"
                + "<p>— Naja7ni Team</p>";
        message.setContent(html, "text/html; charset=utf-8");
        Transport.send(message);
    }

    /** 
     * Public API – request a reset for a given email address.
     * Generates a random 6-digit code for a user and stores it in memory for 10 minutes.
     */
    public void requestReset(String email) throws MessagingException {
        String code = generateCode();
        long expiry = Instant.now().toEpochMilli() + EXPIRY_MILLIS;
        pending.put(email.toLowerCase(), new ResetEntry(code, expiry));
        sendEmail(email, code);
    }

    /** 
     * Verify a submitted code. 
     * Validates if the code entered by the user matches the one sent to their email and checks for expiration.
     * Returns true if the code matches and is not expired. 
     */
    public boolean verifyCode(String email, String code) {
        ResetEntry entry = pending.get(email.toLowerCase());
        if (entry == null) return false;
        if (Instant.now().toEpochMilli() > entry.expiresAt) {
            pending.remove(email.toLowerCase());
            return false;
        }
        boolean ok = entry.code.equals(code);
        if (ok) {
            // consume the code – one‑time use
            pending.remove(email.toLowerCase());
        }
        return ok;
    }
}
