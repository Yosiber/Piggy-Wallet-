package app.web.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listener que realiza un seguimiento del número de sesiones activas en la aplicación.
 * Implementa {@link HttpSessionListener} para detectar la creación y destrucción de sesiones.
 */
@Component
@WebListener
public class ActiveUserSessionListener implements HttpSessionListener {

    /** Contador de sesiones activas en la aplicación. */
    private static final AtomicInteger activeSessions = new AtomicInteger(1);

    /**
     * Incrementa el contador de sesiones activas cuando se crea una nueva sesión.
     *
     * @param se el evento de sesión que contiene información sobre la sesión creada.
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
    }

    /**
     * Decrementa el contador de sesiones activas cuando una sesión es destruida.
     *
     * @param se el evento de sesión que contiene información sobre la sesión destruida.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.decrementAndGet();
    }

    /**
     * Obtiene el número actual de sesiones activas en la aplicación.
     *
     * @return el número de sesiones activas.
     */
    public static int getActiveSessions() {
        return activeSessions.get();
    }
}
