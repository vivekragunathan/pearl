package labs.spring.pearl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = { "labs.spring.pearl" })
@ConfigurationPropertiesScan
public class PearlApplication {
	public static void main(String[] args) {
		SpringApplication.run(PearlApplication.class, args);
	}
}
