package aor.fpbackend.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class StartupBean {
    @PostConstruct
    public void init() {
        createTestUserAndProject();
    }

    private void createTestUserAndProject()  {
        //Create an admin

        // Create a test user

        // Create a test project where the test user is a manager

        // Create a test task in the test project
    }
}
