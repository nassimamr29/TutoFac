package fr.tutofac.etablissement;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etablissements")
public class EtablissementController {

    private final EtablissementService service;

    public EtablissementController(EtablissementService service) {
        this.service = service;
    }

    @GetMapping
    public List<EtablissementDto> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public EtablissementDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtablissementDto> creer(@Valid @RequestBody EtablissementRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EtablissementDto modifier(@PathVariable Long id, @Valid @RequestBody EtablissementRequest r) {
        return service.modifier(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}