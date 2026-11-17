package fr.tutofac.tuteur;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tuteurs")
public class TuteurController {

    private final TuteurService service;

    public TuteurController(TuteurService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TUTEUR')")
    public ProfilTuteurDto monProfil() {
        return service.monProfil();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TUTEUR')")
    public ProfilTuteurDto enregistrerProfil(@Valid @RequestBody ProfilTuteurRequest request) {
        return service.enregistrerProfil(request);
    }

    @PostMapping(value = "/me/releve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TUTEUR')")
    public ProfilTuteurDto deposerReleve(@RequestParam("fichier") MultipartFile fichier) {
        return service.deposerReleve(fichier);
    }

    @GetMapping("/a-valider")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public List<ProfilTuteurDto> profilsEnAttente() {
        return service.profilsEnAttente();
    }

    @PostMapping("/{profilId}/decision")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ProfilTuteurDto decider(@PathVariable Long profilId,
                                   @Valid @RequestBody DecisionProfilRequest request) {
        return service.decider(profilId, request);
    }

    @GetMapping("/{profilId}/releve")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Resource> telechargerReleve(@PathVariable Long profilId) {
        Resource resource = new FileSystemResource(service.relevePourGestionnaire(profilId));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("releve-" + profilId + ".pdf", StandardCharsets.UTF_8)
                        .build().toString())
                .body(resource);
    }
}