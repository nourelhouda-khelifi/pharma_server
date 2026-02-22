package pharmacie.rest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.service.ApprovisionnementService;

@Slf4j
@RestController
@RequestMapping(path = "/api/services/approvisionnement")
public class ApprovisionnementController {

    private final ApprovisionnementService approvisionnementService;

    public ApprovisionnementController(ApprovisionnementService approvisionnementService) {
        this.approvisionnementService = approvisionnementService;
    }

    /**
     * Lance le processus d'approvisionnement :
     * - Détecte les médicaments dont le stock est inférieur au seuil de réapprovisionnement
     * - Envoie un mail à chaque fournisseur concerné avec la liste des médicaments à réapprovisionner
     *
     * @return un résumé de l'opération (nombre de mails envoyés, détails)
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> lancerApprovisionnement() {
        log.info("Contrôleur : lancement du processus d'approvisionnement");
        Map<String, Object> resultat = approvisionnementService.lancerApprovisionnement();
        return ResponseEntity.ok(resultat);
    }
}
