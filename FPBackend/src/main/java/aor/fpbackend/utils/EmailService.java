package aor.fpbackend.utils;

import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@Stateless
public class EmailService {

    private final String USERNAME = "antnestservice@gmail.com";
    private final String PASSWORD = "xnowuqeoyylbxzjw";

    public void sendConfirmationEmail(String toEmail, String confirmationToken) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("antnestservice@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Account Confirmation");
            String htmlMessage = "<html><body>"
                    + "<h1>Confirm Your Account</h1>"
                    + "<p>Thank you for registering. Please click the link below to activate your account:</p>"
                    + "<a href='http://localhost:3000/confirm?token=" + confirmationToken + "' style='padding: 8px 12px; " +
                    "border: 1px solid #007bff; color: white; background-color: #007bff; text-decoration: none;'>Confirm Account</a>"
                    + "</body></html>";
            message.setContent(htmlMessage, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("antnestservice@gmail.com")); // Sender e-mail
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // Receiver e-mail
            message.setSubject("Reset Password"); // E-mail subject
            String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;
            message.setText("You have requested to reset your password. Please click the link below to create a new password:\n" + resetUrl);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
