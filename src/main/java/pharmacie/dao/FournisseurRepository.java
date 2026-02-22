package pharmacie.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pharmacie.entity.Fournisseur;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Integer> {

    /**
     * Trouve tous les fournisseurs qui fournissent une catégorie donnée.
     * @param codeCategorie le code de la catégorie
     * @return la liste des fournisseurs pour cette catégorie
     */
    @Query("SELECT f FROM Fournisseur f JOIN f.categories c WHERE c.code = :codeCategorie")
    List<Fournisseur> findByCategoriesCode(Integer codeCategorie);

    /**
     * Trouve tous les fournisseurs qui fournissent au moins une catégorie
     * contenant des médicaments à réapprovisionner.
     * @return la liste des fournisseurs concernés avec leurs catégories chargées
     */
    @Query("SELECT DISTINCT f FROM Fournisseur f JOIN FETCH f.categories")
    List<Fournisseur> findAllWithCategories();
}
