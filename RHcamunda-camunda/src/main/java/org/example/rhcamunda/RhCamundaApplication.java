package org.example.rhcamunda;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableProcessApplication
public class RhCamundaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhCamundaApplication.class, args);
    }

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private EmployeRepository employeRepository;

    /**
     * ✅ DYNAMIC START: Yji les employés mel base w ybadha l processus lkol wahed.
     * Machi hardcoded!
     */
    @PostDeploy
    public void startProcessOnDeployment() {
        System.out.println("🚀 Starting process instances DYNAMICALLY from database...");

        // ✅ CORRECTION : findByActifTrue() au lieu de findByArchiveFalse()
        List<Employe> employes = employeRepository.findByActifTrue();

        if (employes.isEmpty()) {
            System.out.println("⚠️ No active employees found in database.");
            return;
        }

        System.out.println("📦 Found " + employes.size() + " active employees. Starting processes...");

        // 2. Loop through each employee and start a process instance
        for (Employe employe : employes) {
            try {
                // Prepare variables for this specific employee
                Map<String, Object> variables = new HashMap<>();

                // The key variable: Matricule (will be used by LoadEmployeeDataDelegate)
                variables.put("matricule", employe.getMatricule());

                // Optional: You can add other variables dynamically if needed
                variables.put("startDate", LocalDate.now().plusDays(10));
                variables.put("reason", "Dynamic auto-start");

                // Start the process
                runtimeService.startProcessInstanceByKey("conge-request-process", variables);

                System.out.println("✅ Started process for: " + employe.getFullName() + " (Matricule: " + employe.getMatricule() + ")");

            } catch (Exception e) {
                System.err.println("❌ Failed to start process for employee: " + employe.getMatricule());
                System.err.println("   Error: " + e.getMessage());
            }
        }

        System.out.println("🏁 Dynamic deployment finished!");
    }
}