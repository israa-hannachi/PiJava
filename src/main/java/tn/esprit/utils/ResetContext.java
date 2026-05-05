package tn.esprit.utils;

/**
 * Simple holder for the email address used during a password‑reset flow.
 * It is stored statically only for the short duration of the flow (forgot → verify → set new password).
 * In a production system you would use a more robust session or token mechanism.
 */
public class ResetContext {
    public static String email;
}
