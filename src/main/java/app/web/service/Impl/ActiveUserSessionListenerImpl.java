package app.web.service.Impl;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@WebListener
public class ActiveUserSessionListenerImpl implements HttpSessionListener {
    private static final AtomicInteger activeSessions = new AtomicInteger(1);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.decrementAndGet();
    }

    public static int getActiveSessions() {
        return activeSessions.get();
    }
}
