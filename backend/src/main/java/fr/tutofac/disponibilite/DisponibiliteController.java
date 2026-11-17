package fr.tutofac.disponibilite;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilites")
@PreAuthorize("hasRole('TUTEUR')")
public class DisponibiliteController {

    private final DisponibiliteService service;

    public DisponibiliteController(DisponibiliteService service) {
        this.service = service;
    }

    @GetMapping
    public List<DisponibiliteDto> lister() {
        return service.lister();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibiliteDto creer(@Valid @RequestBody DisponibiliteRequest request) {
        return service.creer(request);
    }

    @PutMapping("/{id}")
    public DisponibiliteDto modifier(@PathVariable Long id,
                                     @Valid @RequestBody DisponibiliteRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}