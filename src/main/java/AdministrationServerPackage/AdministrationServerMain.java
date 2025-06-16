package AdministrationServerPackage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdministrationServerMain {

    public static void main(String[] args) {
        SpringApplication.run(AdministrationServerMain.class, args);
        System.out.println("Administration server running on http://localhost:8080");
    }
}
