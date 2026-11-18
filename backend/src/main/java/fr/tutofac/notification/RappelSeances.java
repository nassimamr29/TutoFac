package fr.tutofac.notification;

import fr.tutofac.seance.Seance;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Tâche planifiée : chaque soir à 18h, rappel par mail des séances du lendemain
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class RappelSeances {

    private final EntityManager entityManager;
    private final NotificationService notificationService;

    public RappelSeances(EntityManager entityManager, NotificationService notificationService) {
        this.entityManager = entityManager;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.mail.rappel-cron:0 0 18 * * *}")
    @Transactional(readOnly = true)
    public void envoyerRappels() {
        LocalDateTime demain = LocalDate.now().plusDays(1).atStartOfDay();
        seancesPlanifiees(demain, demain.plusDays(1)).forEach(notificationService::notifierRappel);
    }

    // Séances encore planifiées (ni annulées, ni réalisées) qui commencent dans [debut, fin[
    List<Seance> seancesPlanifiees(LocalDateTime debut, LocalDateTime fin) {
        return entityManager.createQuery("""
                        select s from Seance s
                        where s.statut = :statut and s.debut >= :debut and s.debut < :fin
                        """, Seance.class)
                .setParameter("statut", Seance.Statut.PLANIFIEE)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }
}
