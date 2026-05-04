package tn.esprit.services.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.participant;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class EmailService {

    private final SmtpConfig config;

    public EmailService(SmtpConfig config) {
        this.config = config;
    }

    public void sendMeetInvitation(participant organizer, List<participant> participants, Meet meet) throws MessagingException {
        if (organizer.getSmtpEmail() == null || organizer.getSmtpAppPassword() == null) {
            throw new MessagingException("L'organisateur n'a pas configuré ses credentials SMTP");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", config.isStartTls());
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", config.getPort());
        props.put("mail.smtp.ssl.trust", config.getHost());

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(organizer.getSmtpEmail(), organizer.getSmtpAppPassword());
            }
        });

        session.setDebug(false);

        for (participant p : participants) {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(organizer.getSmtpEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(p.getEmail()));
            message.setSubject("Invitation: " + meet.getTitre());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String debut = meet.getDateDebut() != null ? meet.getDateDebut().toLocalDateTime().format(formatter) : "N/A";
            String fin = meet.getDateFin() != null ? meet.getDateFin().toLocalDateTime().format(formatter) : "N/A";

            String htmlContent = buildInvitationHtml(organizer, p, meet, debut, fin);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        }
    }

    private String buildInvitationHtml(participant organizer, participant recipient, Meet meet, String debut, String fin) {
        String lien = meet.getLienMeet() != null ? meet.getLienMeet() : "Non spécifié";
        String description = meet.getDescription() != null ? meet.getDescription() : "";
        String recipientName = recipient.getPrenom() + " " + recipient.getNom();
        String organizerName = organizer.getPrenom() + " " + organizer.getNom();

        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n" +
            "        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }\n" +
            "        .header { background: linear-gradient(135deg, #0FB5A9, #04B6D5); padding: 40px 30px; text-align: center; }\n" +
            "        .header h1 { color: white; margin: 0; font-size: 28px; font-weight: 700; }\n" +
            "        .content { padding: 40px 30px; }\n" +
            "        .greeting { font-size: 18px; color: #333; margin-bottom: 20px; }\n" +
            "        .meet-title { font-size: 24px; color: #0FB5A9; font-weight: 700; margin: 20px 0; }\n" +
            "        .detail { background: #f8fafc; padding: 20px; border-radius: 12px; margin: 20px 0; }\n" +
            "        .detail-item { display: flex; margin: 12px 0; }\n" +
            "        .label { font-weight: 600; color: #64748b; width: 100px; }\n" +
            "        .value { color: #1f2937; }\n" +
            "        .link-box { background: #e6fffa; border: 2px solid #0FB5A9; padding: 20px; border-radius: 12px; text-align: center; margin: 20px 0; }\n" +
            "        .link-box a { color: #0FB5A9; font-weight: 700; font-size: 16px; text-decoration: none; }\n" +
            "        .footer { background: #f8fafc; padding: 20px 30px; text-align: center; color: #64748b; font-size: 14px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>📹 Invitation à une réunion</h1>\n" +
            "        </div>\n" +
            "        <div class=\"content\">\n" +
            "            <p class=\"greeting\">Bonjour <strong>" + recipientName + "</strong>,</p>\n" +
            "            <p>Vous êtes invité(e) à participer à une réunion en ligne.</p>\n" +
            "            <div class=\"meet-title\">" + meet.getTitre() + "</div>\n" +
            "            <div class=\"detail\">\n" +
            "                <div class=\"detail-item\">\n" +
            "                    <span class=\"label\">Organisateur:</span>\n" +
            "                    <span class=\"value\">" + organizerName + "</span>\n" +
            "                </div>\n" +
            "                <div class=\"detail-item\">\n" +
            "                    <span class=\"label\">Début:</span>\n" +
            "                    <span class=\"value\">" + debut + "</span>\n" +
            "                </div>\n" +
            "                <div class=\"detail-item\">\n" +
            "                    <span class=\"label\">Fin:</span>\n" +
            "                    <span class=\"value\">" + fin + "</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            <p>" + description + "</p>\n" +
            "            <div class=\"link-box\">\n" +
            "                <p style=\"margin: 0 0 10px 0; color: #64748b;\">Pour rejoindre la réunion:</p>\n" +
            "                <a href=\"" + lien + "\" target=\"_blank\">" + lien + "</a>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"footer\">\n" +
            "            <p>Cette invitation a été envoyée via Naja7ni Meet</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }

    public void sendParticipationConfirmation(participant participant, Meet meet) throws MessagingException {
        if (participant.getSmtpEmail() == null || participant.getSmtpAppPassword() == null) {
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", config.isStartTls());
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", config.getPort());

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(participant.getSmtpEmail(), participant.getSmtpAppPassword());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(participant.getSmtpEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(participant.getEmail()));
        message.setSubject("Confirmation d'inscription: " + meet.getTitre());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String debut = meet.getDateDebut() != null ? meet.getDateDebut().toLocalDateTime().format(formatter) : "N/A";

        String htmlContent = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n" +
            "        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }\n" +
            "        .header { background: linear-gradient(135deg, #0FB5A9, #04B6D5); padding: 30px; text-align: center; }\n" +
            "        .header h1 { color: white; margin: 0; font-size: 24px; }\n" +
            "        .content { padding: 30px; }\n" +
            "        .success-icon { font-size: 48px; text-align: center; margin: 20px 0; }\n" +
            "        .meet-title { font-size: 20px; color: #0FB5A9; font-weight: 700; text-align: center; margin: 20px 0; }\n" +
            "        .detail { background: #f8fafc; padding: 20px; border-radius: 12px; margin: 20px 0; }\n" +
            "        .link-box { background: #e6fffa; border: 2px solid #0FB5A9; padding: 20px; border-radius: 12px; text-align: center; }\n" +
            "        .link-box a { color: #0FB5A9; font-weight: 700; text-decoration: none; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>✅ Confirmation d'inscription</h1>\n" +
            "        </div>\n" +
            "        <div class=\"content\">\n" +
            "            <div class=\"success-icon\">🎉</div>\n" +
            "            <p style=\"text-align: center; color: #64748b;\">Vous êtes maintenant inscrit(e) à:</p>\n" +
            "            <div class=\"meet-title\">" + meet.getTitre() + "</div>\n" +
            "            <div class=\"detail\">\n" +
            "                <p><strong>Date:</strong> " + debut + "</p>\n" +
            "            </div>\n" +
            "            <div class=\"link-box\">\n" +
            "                <a href=\"" + meet.getLienMeet() + "\" target=\"_blank\">Rejoindre la réunion</a>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

        message.setContent(htmlContent, "text/html; charset=utf-8");
        Transport.send(message);
    }
}
