package pharmacie.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.FournisseurRepository;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

@Slf4j
@Service
public class ApprovisionnementService {

    private final MedicamentRepository medicamentDao;
    private final FournisseurRepository fournisseurDao;
    private final EmailSender emailSender;

    public ApprovisionnementService(
            MedicamentRepository medicamentDao,
            FournisseurRepository fournisseurDao,
            EmailSender emailSender) {
        this.medicamentDao = medicamentDao;
        this.fournisseurDao = fournisseurDao;
        this.emailSender = emailSender;
    }

    /**
     * Service métier d'approvisionnement :
     * 1. Détermine les médicaments à réapprovisionner (unitesEnStock < niveauDeReappro)
     * 2. Envoie un mail personnalisé à chaque fournisseur concerné
     *
     * @return un résumé de l'opération (nombre de mails envoyés, détails)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> lancerApprovisionnement() {
        log.info("Service : Lancement du processus d'approvisionnement");

        // Étape 1 : Trouver les médicaments à réapprovisionner
        List<Medicament> tousLesMedicaments = medicamentDao.findAll();
        List<Medicament> aReapprovisionner = tousLesMedicaments.stream()
                .filter(m -> m.getUnitesEnStock() < m.getNiveauDeReappro())
                .toList();

        if (aReapprovisionner.isEmpty()) {
            log.info("Aucun médicament à réapprovisionner");
            return Map.of(
                "message", "Aucun médicament à réapprovisionner",
                "mailsEnvoyes", 0,
                "medicamentsAReapprovisionner", 0
            );
        }

        log.info("{} médicament(s) à réapprovisionner", aReapprovisionner.size());

        // Étape 2 : Grouper les médicaments par catégorie
        Map<Categorie, List<Medicament>> parCategorie = aReapprovisionner.stream()
                .collect(Collectors.groupingBy(Medicament::getCategorie));

        // Étape 3 : Pour chaque fournisseur, déterminer les médicaments qu'il peut fournir
        List<Fournisseur> tousLesFournisseurs = fournisseurDao.findAllWithCategories();

        int mailsEnvoyes = 0;
        List<Map<String, Object>> detailsMails = new ArrayList<>();

        for (Fournisseur fournisseur : tousLesFournisseurs) {
            // Trouver les catégories du fournisseur qui ont des médicaments à réapprovisionner
            Map<String, List<String>> categoriesMedicaments = new LinkedHashMap<>();

            for (Categorie categorie : fournisseur.getCategories()) {
                List<Medicament> medicaments = parCategorie.get(categorie);
                if (medicaments != null && !medicaments.isEmpty()) {
                    List<String> nomsMedicaments = medicaments.stream()
                            .map(m -> String.format("  - %s (stock: %d, seuil: %d)",
                                    m.getNom(), m.getUnitesEnStock(), m.getNiveauDeReappro()))
                            .toList();
                    categoriesMedicaments.put(categorie.getLibelle(), nomsMedicaments);
                }
            }

            // Si ce fournisseur a des médicaments à réapprovisionner, on lui envoie un mail
            if (!categoriesMedicaments.isEmpty()) {
                String contenuMail = construireMail(fournisseur, categoriesMedicaments);
                envoyerMail(fournisseur, contenuMail);
                mailsEnvoyes++;

                detailsMails.add(Map.of(
                    "fournisseur", fournisseur.getNom(),
                    "email", fournisseur.getEmail(),
                    "categoriesConcernees", categoriesMedicaments.keySet()
                ));
            }
        }

        log.info("{} mail(s) envoyé(s)", mailsEnvoyes);

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("message", "Approvisionnement lancé avec succès");
        resultat.put("medicamentsAReapprovisionner", aReapprovisionner.size());
        resultat.put("mailsEnvoyes", mailsEnvoyes);
        resultat.put("details", detailsMails);
        return resultat;
    }

    /**
     * Construit le contenu du mail pour un fournisseur donné
     */
    private String construireMail(Fournisseur fournisseur, Map<String, List<String>> categoriesMedicaments) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(fournisseur.getNom()).append(",\n\n");
        sb.append("Nous vous contactons car certains médicaments de nos stocks nécessitent un réapprovisionnement.\n");
        sb.append("Vous trouverez ci-dessous la liste des médicaments concernés, classés par catégorie :\n\n");

        for (Map.Entry<String, List<String>> entry : categoriesMedicaments.entrySet()) {
            sb.append("=== ").append(entry.getKey()).append(" ===\n");
            for (String medicament : entry.getValue()) {
                sb.append(medicament).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Merci de bien vouloir nous transmettre un devis de réapprovisionnement pour ces médicaments.\n\n");
        sb.append("Cordialement,\n");
        sb.append("La Pharmacie Centrale");

        return sb.toString();
    }

    /**
     * Envoie un mail au fournisseur via l'EmailSender injecté
     * (SMTP en dev, SendGrid en production)
     */
    private void envoyerMail(Fournisseur fournisseur, String contenu) {
        try {
            emailSender.envoyerEmail(
                fournisseur.getEmail(),
                "Demande de devis de réapprovisionnement - Pharmacie Centrale",
                contenu
            );
            log.info("Mail envoyé à {} ({})", fournisseur.getNom(), fournisseur.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du mail à {} : {}", fournisseur.getEmail(), e.getMessage());
            // On ne bloque pas le processus si un mail échoue
        }
    }
}
