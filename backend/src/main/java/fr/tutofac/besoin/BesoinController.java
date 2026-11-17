package fr.tutofac.besoin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/besoins")
@PreAuthorize("hasRole('GESTIONNAIRE')")
public class BesoinController {

    private final BesoinService service;

    public BesoinController(BesoinService service) {
        this.service = service;
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

    // Crée un nouveau besoin
    @PostMapping
    public ResponseEntity<BesoinDto> creer(
            @Valid @RequestBody BesoinRequest request) {

        BesoinDto besoin = service.creer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(besoin);
    }

    // Annule un besoin
    @PatchMapping("/{id}/annuler")
    public BesoinDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}