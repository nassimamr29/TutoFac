package fr.tutofac;

import fr.tutofac.notification.NotificationService;
import fr.tutofac.notification.RappelSeances;
import fr.tutofac.seance.Seance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RappelSeancesTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Seance> requete;

    @Mock
    private NotificationService notificationService;

    @Test
    void unRappelEstEnvoyePourChaqueSeancePlanifieeDuLendemain() {
        Seance s1 = new Seance();
        Seance s2 = new Seance();
        when(entityManager.createQuery(anyString(), eq(Seance.class))).thenReturn(requete);
        when(requete.setParameter(anyString(), any())).thenReturn(requete);
        when(requete.getResultList()).thenReturn(List.of(s1, s2));

        new RappelSeances(entityManager, notificationService).envoyerRappels();

        // seulement les séances PLANIFIEE qui commencent demain (entre 0h et 24h)
        verify(requete).setParameter("statut", Seance.Statut.PLANIFIEE);
        verify(requete).setParameter("debut", LocalDate.now().plusDays(1).atStartOfDay());
        verify(requete).setParameter("fin", LocalDate.now().plusDays(2).atStartOfDay());
        verify(notificationService).notifierRappel(s1);
        verify(notificationService).notifierRappel(s2);
    }
}
