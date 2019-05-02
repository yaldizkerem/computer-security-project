package hello;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
        return "<a href=\"/computer-security\">Greetings from Spring Boot!</a>";
    }

    @RequestMapping("/computer-security")
    public String computerSecurity() {
        return "Greetings from Spring Boot!";
    }
    
}
