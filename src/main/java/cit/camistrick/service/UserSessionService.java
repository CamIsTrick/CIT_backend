package cit.camistrick.service;

import cit.camistrick.domain.UserSession;
import cit.camistrick.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserSessionService implements AutoCloseable {

    private final UserSessionRepository userSessionRepository;

    public UserSession findSession(String sessionId) {
        return findUserSessionById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Cannot find target user session with ID: " + sessionId));
    }

    public void removeSession(String sessionId) {
        findUserSessionById(sessionId).ifPresent(userSession -> {
            userSessionRepository.remove(sessionId);
            log.info("Session removed: {}", sessionId);
        });
    }

    public void register(UserSession user) {
        userSessionRepository.add(user.getSessionId(), user);
        log.info("New user registered - ID: {}, User: {}", user.getSessionId(), user);
    }

    public List<UserSession> findAllUserSession() {
        return userSessionRepository.getAllSession();
    }

    @Override
    public void close() {
        userSessionRepository.getAllSession().forEach(UserSession::close);
        log.info("All UserSessions closed");
    }

    @PreDestroy
    private void shutdown() {
        log.info("Shutting down, closing all UserSessions");
        close();
    }

    private Optional<UserSession> findUserSessionById(String sessionId) {
        return Optional.ofNullable(userSessionRepository.findBySessionId(sessionId));
    }
}