package com.example.firstservice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/first-service")
public class FirstServiceController {

    Environment environment;

    @Autowired
    public FirstServiceController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome, This is First Service";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("first-request") String header){
        log.info(header);
        return "FirstServiceController.message";
    }

    @GetMapping("/check")
    public String check(HttpServletRequest request) {
        log.info("Server Port: {}", request.getServerPort());
        return String.format("FirstServiceController.check / PORT: %s", environment.getProperty("local.server.port"));
    }

}
