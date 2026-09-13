package world.creve;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling public class CreveApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreveApplication.class, args);
    }
}
