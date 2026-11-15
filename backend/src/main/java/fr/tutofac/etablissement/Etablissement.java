package fr.tutofac.etablissement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "etablissement")
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEtablissement type;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_partenariat", nullable = false)
    private StatutPartenariat statutPartenariat = StatutPartenariat.ACTIF;

    // rempli par la base (DEFAULT now())
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public TypeEtablissement getType() { return type; }
    public void setType(TypeEtablissement type) { this.type = type; }
    public StatutPartenariat getStatutPartenariat() { return statutPartenariat; }
    public void setStatutPartenariat(StatutPartenariat s) { this.statutPartenariat = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}