package fr.tutofac.disponibilite;

import fr.tutofac.tuteur.ProfilTuteur;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "disponibilite")
public class Disponibilite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profil_tuteur_id", nullable = false)
    private ProfilTuteur profilTuteur;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recurrence recurrence = Recurrence.AUCUNE;

    @Column(name = "fin_recurrence")
    private LocalDate finRecurrence;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProfilTuteur getProfilTuteur() { return profilTuteur; }
    public void setProfilTuteur(ProfilTuteur profilTuteur) { this.profilTuteur = profilTuteur; }
    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime debut) { this.debut = debut; }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
    public Recurrence getRecurrence() { return recurrence; }
    public void setRecurrence(Recurrence recurrence) { this.recurrence = recurrence; }
    public LocalDate getFinRecurrence() { return finRecurrence; }
    public void setFinRecurrence(LocalDate finRecurrence) { this.finRecurrence = finRecurrence; }
}