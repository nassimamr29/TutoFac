package fr.tutofac.matiere;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matieres")
public class MatiereController {

    private final MatiereRepository matieres;

    public MatiereController(MatiereRepository matieres) {
        this.matieres = matieres;
    }

    @GetMapping
    public List<Matiere> lister() {
        return matieres.findAllByOrderByDomaineAscIntituleAscNiveauAsc();
    }
}
