package team.project.upb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import team.project.upb.api.config.CustomPasswordEncoder;

@SpringBootApplication
public class UpbApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpbApplication.class, args);
	}

	@Bean
	public CustomPasswordEncoder bCryptPasswordEncoder() {
		return new CustomPasswordEncoder();
	}

}
