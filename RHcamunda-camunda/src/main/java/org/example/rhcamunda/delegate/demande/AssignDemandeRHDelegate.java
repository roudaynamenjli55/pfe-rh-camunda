package org.example.rhcamunda.delegate.demande;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignDemandeRHDelegate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String rhMatricule = (String) delegateTask.getVariable("rhMatricule");

        // Assigner automatiquement au RH
        delegateTask.setAssignee(rhMatricule);
        log.info("✅ Demande assignée au RH: {}", rhMatricule);
    }
}