package fr.tutofac.etablissement;

import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EtablissementService {

    private final EtablissementRepository repo;

    public EtablissementService(EtablissementRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<EtablissementDto> lister() {
        return repo.findAll(Sort.by("nom")).stream().map(EtablissementDto::from).toList();
    }

    @Transactional(readOnly = true)
    public EtablissementDto trouver(Long id) {
        return EtablissementDto.from(chercher(id));
    }

    @Transactional
    public EtablissementDto creer(EtablissementRequest r) {
        if (repo.existsByNom(r.nom())) {
            throw new ConflictException("Un établissement porte déjà ce nom");
        }
        Etablissement e = new Etablissement();
        remplir(e, r);
        return EtablissementDto.from(repo.save(e));
    }

    @Transactional
    public EtablissementDto modifier(Long id, EtablissementRequest r) {
        Etablissement e = chercher(id);
        if (!e.getNom().equals(r.nom()) && repo.existsByNom(r.nom())) {
            throw new ConflictException("Un établissement porte déjà ce nom");
        }
        remplir(e, r);
        return EtablissementDto.from(repo.save(e));
    }

    @Transactional
    public void supprimer(Long id) {
        chercher(id);
        repo.deleteById(id);
        repo.flush(); // déclenche l'erreur tout de suite si l'établissement est référencé
    }

    private Etablissement chercher(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Établissement introuvable"));
    }

    private void remplir(Etablissement e, EtablissementRequest r) {
        e.setNom(r.nom().trim());
        e.setType(r.type());
        if (r.statutPartenariat() != null) {
            e.setStatutPartenariat(r.statutPartenariat());
        }
    }
}