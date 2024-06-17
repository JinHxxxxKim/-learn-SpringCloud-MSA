package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/")
@Slf4j
public class UserController {

    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;


    @GetMapping("/health_check")
    public String status() {
        log.info("UserController.status");
        return String.format("Working in [USER-SERVICE]\n"
                + "port(local.server.port): " + environment.getProperty("local.server.port") + "\n"
                + "port(server.port): " + environment.getProperty("server.port") + "\n"
                + "jwt secret: " + environment.getProperty("jwt.secret") + "\n"
                + "jwt expTime: " + environment.getProperty("jwt.expiration_time") + "\n");

    }

    @GetMapping("/welcome")
    public String welcome() {
//        return environment.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RequestUser user) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = mapper.map(user, UserDto.class);

        log.info("before userDto: {}", userDto);
        userService.createUser(userDto);
        log.info("after userDto: {}", userDto);
        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        log.info("UserController.getUsers");
        Iterable<UserEntity> users = userService.getUserByAll();
        List<ResponseUser> responseUsers = new ArrayList<>();
        users.forEach(v -> {
            responseUsers.add(new ModelMapper().map(v, ResponseUser.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(responseUsers);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        log.info("UserController.getUser");
        UserDto user = userService.getUserByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(user, ResponseUser.class));
    }
}
