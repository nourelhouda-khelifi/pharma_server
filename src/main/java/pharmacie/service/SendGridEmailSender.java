package pharmacie.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Implémentation SendGrid pour l'envoi d'emails.
 * Utilisée en local et en production.
 * SendGrid fournit une API HTTP (pas de SMTP), compatible avec Render.com.
 */
@Slf4j
@Component
public class SendGridEmailSender implements EmailSender {

    @Value("${spring.sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:pharmacie-centrale@example.com}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Pharmacie Centrale}")
    private String fromName;

    @Override
    public void envoyerEmail(String destinataire, String sujet, String contenu) throws Exception {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(destinataire);
        Content content = new Content("text/plain", contenu);
        Mail mail = new Mail(from, sujet, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        log.info("[SendGrid] Réponse pour {} : status={}, body={}, headers={}",
                destinataire, response.getStatusCode(), response.getBody(), response.getHeaders());

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            log.info("[SendGrid] Mail envoyé à {} (status: {})", destinataire, response.getStatusCode());
        } else {
            log.error("[SendGrid] Erreur envoi à {} (status: {}, body: {})",
                    destinataire, response.getStatusCode(), response.getBody());
            throw new RuntimeException("SendGrid error: " + response.getStatusCode());
        }
    }
}
