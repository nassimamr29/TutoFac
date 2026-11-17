package fr.tutofac.tuteur;

import fr.tutofac.matiere.Matiere;
import fr.tutofac.matiere.NiveauEtudes;
import fr.tutofac.utilisateur.Utilisateur;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "profil_tuteur")
public class ProfilTuteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String formation;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_etudes", nullable = false)
    private NiveauEtudes niveauEtudes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProfil statut = StatutProfil.EN_ATTENTE;

    @Column(name = "releve_path")
    private String relevePath;

    @Column(name = "motif_refus")
    private String motifRefus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par")
    private Utilisateur validePar;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(name = "profil_tuteur_matiere",
            joinColumns = @JoinColumn(name = "profil_tuteur_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    private Set<Matiere> matieres = new LinkedHashSet<>();

    public Long getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }
    public NiveauEtudes getNiveauEtudes() { return niveauEtudes; }
    public void setNiveauEtudes(NiveauEtudes niveauEtudes) { this.niveauEtudes = niveauEtudes; }
    public StatutProfil getStatut() { return statut; }
    public void setStatut(StatutProfil statut) { this.statut = statut; }
    public String getRelevePath() { return relevePath; }
    public void setRelevePath(String relevePath) { this.relevePath = relevePath; }
    public String getMotifRefus() { return motifRefus; }
    public void setMotifRefus(String motifRefus) { this.motifRefus = motifRefus; }
    public Utilisateur getValidePar() { return validePar; }
    public void setValidePar(Utilisateur validePar) { this.validePar = validePar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Matiere> getMatieres() { return matieres; }
    public void setMatieres(Set<Matiere> matieres) { this.matieres = matieres; }
}