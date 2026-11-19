package com.hospital;

import com.hospital.ui.ConsoleMenu;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Entry point. @SpringBootApplication turns on component scanning, so
 * Spring finds and creates all the beans (@Repository, @Service,
 * @Component) and wires them together with dependency injection.
 * main() then starts the console menu explicitly.
 */
@SpringBootApplication
public class HospitalApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(HospitalApplication.class, args);
        context.getBean(ConsoleMenu.class).start();
        context.close();
    }
}
