package fr.tutofac.matiere;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    List<Matiere> findAllByOrderByDomaineAscIntituleAscNiveauAsc();
}
