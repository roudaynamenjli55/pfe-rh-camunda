package org.example.rhcamunda.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;  // ✅ Hedhi s7i7a (jakarta w machi javax)
import java.io.IOException;

@Service
public class ProcessDeploymentService {

    @Autowired
    private RepositoryService repositoryService;

    @PostConstruct
    public void deployAllProcesses() {
        System.out.println("🚀 === Starting Dynamic Deployment ===");

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:processes/**/*.bpmn");

            System.out.println("📂 Found " + resources.length + " BPMN files in /resources/processes/");

            if (resources.length > 0) {
                var deploymentBuilder = repositoryService.createDeployment()
                        .name("Dynamic Deployment - All Processes")
                        .source("rhcamunda-application");

                for (Resource resource : resources) {
                    String filename = resource.getFilename();
                    System.out.println("📥 Adding process: " + filename);

                    deploymentBuilder.addInputStream(
                            filename,
                            resource.getInputStream()
                    );
                }

                Deployment deployment = deploymentBuilder.deploy();
                System.out.println("✅ Successfully deployed: " + deployment.getName() + " (ID: " + deployment.getId() + ")");

            } else {
                System.out.println("⚠️ No BPMN files found in /resources/processes/");
            }

        } catch (IOException e) {
            System.err.println("❌ Error during deployment: " + e.getMessage());
            e.printStackTrace();
        }
    }
}