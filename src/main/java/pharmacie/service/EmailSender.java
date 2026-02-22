package pharmacie.service;

/**
 * Interface pour l'envoi d'emails.
 * Deux implémentations :
 * - SmtpEmailSender : utilise Spring Mail (SMTP Gmail) en développement local
 * - SendGridEmailSender : utilise l'API SendGrid en production (Render bloque SMTP)
 */
public interface EmailSender {

    /**
     * Envoie un email.
     * @param destinataire l'adresse email du destinataire
     * @param sujet le sujet du mail
     * @param contenu le contenu texte du mail
     * @throws Exception en cas d'erreur d'envoi
     */
    void envoyerEmail(String destinataire, String sujet, String contenu) throws Exception;
}
