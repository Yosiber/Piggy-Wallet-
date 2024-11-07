package app.web.controller;

import app.web.Service.UserService;
import app.web.response.SessionResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;


    @GetMapping("/session")
    public ResponseEntity getCurrentSession() {
        SessionResponse response = new SessionResponse();
        return ResponseEntity.ok(response);
    }
}
