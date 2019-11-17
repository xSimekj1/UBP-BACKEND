package team.project.upb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import team.project.upb.api.security.CustomPasswordEncoder;

@Controller
@SpringBootApplication
public class UpbApplication implements ErrorController {

	public static void main(String[] args) {
		SpringApplication.run(UpbApplication.class, args);
	}

	@Bean
	public CustomPasswordEncoder bCryptPasswordEncoder() {
		return new CustomPasswordEncoder();
	}

	private static final String PATH = "/error";

	@RequestMapping(value = PATH)
	public String error() {
		return "forward:/index.html";
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}

}
