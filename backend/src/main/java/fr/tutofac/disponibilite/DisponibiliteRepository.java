package fr.tutofac.disponibilite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {
    List<Disponibilite> findByProfilTuteurUtilisateurIdOrderByDebutAsc(Long utilisateurId);
}