package pharmacie.entity;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Fournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
    private Integer id;

    @NonNull
    @NotBlank
    @Size(min = 1, max = 255)
    @Column(unique = true, nullable = false, length = 255)
    private String nom;

    @NonNull
    @NotBlank
    @Email
    @Size(min = 1, max = 255)
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    /**
     * Un fournisseur peut fournir les médicaments de plusieurs catégories.
     * La relation est bidirectionnelle, le côté "propriétaire" est ici (Fournisseur).
     * JPA va créer une table de jointure FOURNISSEUR_CATEGORIES.
     */
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
        name = "FOURNISSEUR_CATEGORIES",
        joinColumns = @JoinColumn(name = "FOURNISSEUR_ID"),
        inverseJoinColumns = @JoinColumn(name = "CATEGORIE_CODE")
    )
    @JsonIgnoreProperties({"medicaments", "fournisseurs"})
    private List<Categorie> categories = new LinkedList<>();
}
