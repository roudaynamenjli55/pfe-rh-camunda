package org.example.rhcamunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotifyRHDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyRHDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String employeeId = (String) execution.getVariable("employeeId");
        String startDate = (String) execution.getVariable("startDate");
        String endDate = (String) execution.getVariable("endDate");

        LOGGER.info("📧 Notification RH: Congé approuvé pour l'employé {}", employeeId);
        LOGGER.info("📅 Période: {} au {}", startDate, endDate);

        execution.setVariable("rhNotified", true);
        execution.setVariable("notificationDate", new java.util.Date());
    }
}