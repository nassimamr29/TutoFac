package fr.tutofac.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Textes des e-mails envoyés par la plateforme (texte brut, sans HTML)
public final class EmailTemplates {

    public record Email(String sujet, String corps) {}

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm");

    private EmailTemplates() {}

    public static Email affectationTuteur(String prenom, LocalDateTime debut, LocalDateTime fin, String lieu) {
        return new Email(
                "TutoFac : nouvelle séance de tutorat le " + debut.format(DATE),
                "Bonjour " + prenom + ",\n\n"
                        + "Vous avez été affecté(e) à une séance de tutorat.\n\n"
                        + creneau(debut, fin, lieu)
                        + "\nRetrouvez le détail de vos séances sur TutoFac.\n\n"
                        + signature());
    }

    public static Email affectationEtudiant(String prenom, LocalDateTime debut, LocalDateTime fin, String lieu) {
        return new Email(
                "TutoFac : vous êtes inscrit(e) à une séance de tutorat le " + debut.format(DATE),
                "Bonjour " + prenom + ",\n\n"
                        + "Votre établissement vous a inscrit(e) à une séance de tutorat.\n\n"
                        + creneau(debut, fin, lieu)
                        + "\nMerci de prévenir votre établissement en cas d'absence.\n\n"
                        + signature());
    }

    private static String creneau(LocalDateTime debut, LocalDateTime fin, String lieu) {
        String lieuAffiche = (lieu == null || lieu.isBlank()) ? "à préciser" : lieu;
        return "Date : " + debut.format(DATE) + "\n"
                + "Horaire : " + debut.format(HEURE) + " - " + fin.format(HEURE) + "\n"
                + "Lieu ou lien : " + lieuAffiche + "\n";
    }

    private static String signature() {
        return "L'équipe TutoFac\n(message automatique, merci de ne pas répondre)";
    }
}
