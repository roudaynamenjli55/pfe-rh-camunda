package org.example.rhcamunda.delegate.autorisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("notificationDelegate")
@RequiredArgsConstructor
@Slf4j
public class NotificationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String matricule = (String) execution.getVariable("employeMatricule");
        String decision = (String) execution.getVariable("decisionManager");
        String commentaire = (String) execution.getVariable("commentaireManager");

        log.info("📧 Notification envoyée à {}: décision={}, commentaire={}",
                matricule, decision, commentaire);

        // TODO: Intégrer un service d'envoi d'email ou de notification push
        // emailService.sendNotification(matricule, decision, commentaire);
    }
}