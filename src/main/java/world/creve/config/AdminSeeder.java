package world.creve.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import world.creve.service.AdminService;

@Component
public class AdminSeeder implements CommandLineRunner {
  private final AdminService adminService;
  private final String adminEmail;
  private final String adminPassword;

  public AdminSeeder(
      AdminService adminService,
      @Value("${creve.admin.email:admin@creve.world}") String adminEmail,
      @Value("${creve.admin.password:playpit-admin}") String adminPassword
  ) {
    this.adminService = adminService;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(String... args) {
    adminService.ensureDefaultAdmin(adminEmail, adminPassword);
  }
}
