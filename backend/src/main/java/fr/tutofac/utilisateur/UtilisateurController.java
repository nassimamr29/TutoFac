package fr.tutofac.utilisateur;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UtilisateurDto moi() {
        return service.moi();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE')")
    public List<UtilisateurDto> lister() {
        return service.lister();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurDto> creer(@Valid @RequestBody UtilisateurRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(r));
    }
}