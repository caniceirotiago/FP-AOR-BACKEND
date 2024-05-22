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
            String confirmButton = "<html><body>"
                    + "<h1>Confirm Your Account</h1>"
                    + "<p>Thank you for registering. Please click the button below to activate your account:</p>"
                    + "<table cellspacing=\"0\" cellpadding=\"0\"><tr><td>"
                    + "<a href='http://localhost:3000/confirm?token=" + confirmationToken + "' style='background-color:#007bff;border:1px solid #007bff;border-radius:5px;color:#ffffff;display:inline-block;font-family:sans-serif;font-size:16px;line-height:44px;text-align:center;text-decoration:none;width:200px;-webkit-text-size-adjust:none;mso-hide:all;'>Confirm Account</a>"
                    + "</td></tr></table>"
                    + "</body></html>";
            message.setContent(confirmButton, "text/html; charset=utf-8");
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
            String resetButton = "<html><body>"
                    + "<h1>Reset Your Password</h1>"
                    + "<p>You have requested to reset your password. Please click the button below to create a new password:</p>"
                    + "<table cellspacing=\"0\" cellpadding=\"0\"><tr><td>"
                    + "<a href='" + resetUrl + "' style='background-color:#007bff;border:1px solid #007bff;border-radius:5px;color:#ffffff;display:inline-block;font-family:sans-serif;font-size:16px;line-height:44px;text-align:center;text-decoration:none;width:200px;-webkit-text-size-adjust:none;mso-hide:all;'>Reset Password</a>"
                    + "</td></tr></table>"
                    + "</body></html>";
            message.setContent(resetButton, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
