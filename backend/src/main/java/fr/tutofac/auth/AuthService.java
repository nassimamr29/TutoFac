package fr.tutofac.auth;

import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurs, PasswordEncoder encoder, JwtService jwtService) {
        this.utilisateurs = utilisateurs;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest r) {
        // même message dans tous les cas : on ne révèle pas si l'email existe
        Utilisateur u = utilisateurs.findByEmail(r.email().trim().toLowerCase())
                .filter(Utilisateur::isActif)
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));
        if (!encoder.matches(r.motDePasse(), u.getMotDePasse())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
        var etab = u.getEtablissement();
        return new LoginResponse(jwtService.generateToken(u), u.getRole(),
                etab == null ? null : etab.getId(), etab == null ? null : etab.getNom());
    }
}