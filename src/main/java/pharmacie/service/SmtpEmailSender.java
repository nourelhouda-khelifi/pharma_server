package pharmacie.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Implémentation SMTP (Spring Mail) pour l'envoi d'emails.
 * Désactivée par défaut. Activable via spring.mail.enabled=true dans application.properties.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.mail.enabled", havingValue = "true", matchIfMissing = false)
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    public SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void envoyerEmail(String destinataire, String sujet, String contenu) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(contenu);

        mailSender.send(message);
        log.info("[SMTP] Mail envoyé à {}", destinataire);
    }
}
