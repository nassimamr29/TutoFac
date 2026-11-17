package fr.tutofac.besoin;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/besoins")
@PreAuthorize("hasRole('GESTIONNAIRE')")
public class BesoinController {

    private final BesoinService service;
    private final RechercheTuteurService rechercheService;

    public BesoinController(
            BesoinService service,
            RechercheTuteurService rechercheService) {
        this.service = service;
        this.rechercheService = rechercheService;
    }

    // Liste les besoins de l'établissement connecté
    @GetMapping
    public List<BesoinDto> lister() {
        return service.lister();
    }

    // Consulte un besoin
    @GetMapping("/{id}")
    public BesoinDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    // Crée un besoin
    @PostMapping
    public ResponseEntity<BesoinDto> creer(
            @Valid @RequestBody BesoinRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.creer(request));
    }

    // Annule un besoin
    @PatchMapping("/{id}/annuler")
    public BesoinDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    // Recherche les tuteurs disponibles pour un besoin et un créneau
    @GetMapping("/{id}/tuteurs")
    public List<TuteurRechercheDto> rechercherTuteurs(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime debut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin) {

        return rechercheService.rechercher(id, debut, fin);
    }
}