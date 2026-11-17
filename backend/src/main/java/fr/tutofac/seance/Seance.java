package fr.tutofac.seance;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seance")
public class Seance {

    public enum Statut {
        PLANIFIEE, REALISEE, ANNULEE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "besoin_id", nullable = false)
    private Long besoinId;

    @Column(name = "profil_tuteur_id", nullable = false)
    private Long profilTuteurId;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(name = "lieu_ou_lien")
    private String lieuOuLien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.PLANIFIEE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBesoinId() {
        return besoinId;
    }

    public void setBesoinId(Long besoinId) {
        this.besoinId = besoinId;
    }

    public Long getProfilTuteurId() {
        return profilTuteurId;
    }

    public void setProfilTuteurId(Long profilTuteurId) {
        this.profilTuteurId = profilTuteurId;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public String getLieuOuLien() {
        return lieuOuLien;
    }

    public void setLieuOuLien(String lieuOuLien) {
        this.lieuOuLien = lieuOuLien;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}