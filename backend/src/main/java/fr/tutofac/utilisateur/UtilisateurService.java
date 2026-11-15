package fr.tutofac.utilisateur;

import fr.tutofac.auth.CurrentUser;
import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.etablissement.Etablissement;
import fr.tutofac.etablissement.EtablissementRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurs;
    private final EtablissementRepository etablissements;
    private final PasswordEncoder encoder;

    public UtilisateurService(UtilisateurRepository utilisateurs,
                              EtablissementRepository etablissements,
                              PasswordEncoder encoder) {
        this.utilisateurs = utilisateurs;
        this.etablissements = etablissements;
        this.encoder = encoder;
    }

    public UtilisateurDto moi() {
        return UtilisateurDto.from(CurrentUser.get());
    }

    /** ADMIN : tous les comptes. GESTIONNAIRE : ceux de son établissement. */
    @Transactional(readOnly = true)
    public List<UtilisateurDto> lister() {
        List<Utilisateur> liste = CurrentUser.isAdmin()
                ? utilisateurs.findAll()
                : utilisateurs.findByEtablissementId(CurrentUser.etablissementId());
        return liste.stream().map(UtilisateurDto::from).toList();
    }

    @Transactional
    public UtilisateurDto creer(UtilisateurRequest r) {
        String email = r.email().trim().toLowerCase();
        if (utilisateurs.existsByEmail(email)) {
            throw new ConflictException("Cet email est déjà utilisé");
        }
        Etablissement etab = null;
        if (r.role() != Role.ADMIN) {
            if (r.etablissementId() == null) {
                throw new IllegalArgumentException("Un établissement est obligatoire pour ce rôle");
            }
            etab = etablissements.findById(r.etablissementId())
                    .orElseThrow(() -> new NotFoundException("Établissement introuvable"));
        }
        Utilisateur u = new Utilisateur();
        u.setNom(r.nom().trim());
        u.setPrenom(r.prenom().trim());
        u.setEmail(email);
        u.setMotDePasse(encoder.encode(r.motDePasse()));
        u.setRole(r.role());
        u.setEtablissement(etab);
        return UtilisateurDto.from(utilisateurs.save(u));
    }
}