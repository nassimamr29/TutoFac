package fr.tutofac.seance;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seances")
@PreAuthorize("hasRole('GESTIONNAIRE')")
public class SeanceController {

    private final SeanceService service;

    public SeanceController(SeanceService service) {
        this.service = service;
    }

    // Liste les séances de l'établissement connecté
    @GetMapping
    public List<SeanceDto> lister() {
        return service.lister();
    }

    // Consulte une séance
    @GetMapping("/{id}")
    public SeanceDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    // Liste les séances associées à un besoin
    @GetMapping("/besoin/{besoinId}")
    public List<SeanceDto> listerParBesoin(@PathVariable Long besoinId) {
        return service.listerParBesoin(besoinId);
    }

    // Crée une séance avec un tuteur et des étudiants
    @PostMapping
    public ResponseEntity<SeanceDto> creer(
            @Valid @RequestBody SeanceRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.creer(request));
    }

    // Annule une séance
    @PatchMapping("/{id}/annuler")
    public SeanceDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    // Marque une séance comme réalisée
    @PatchMapping("/{id}/realiser")
    public SeanceDto realiser(@PathVariable Long id) {
        return service.realiser(id);
    }
}   