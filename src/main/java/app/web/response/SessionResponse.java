package app.web.response;

import app.web.persistence.entities.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SessionResponse {
    private UserEntity user;
    private LocalDateTime loginTime;
    private String sessionId;
    private List<String> authorities;
    private boolean active;
}