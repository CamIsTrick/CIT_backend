package cit.camistrick.repository;

import cit.camistrick.domain.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class MemoryUserSessionRepository implements UserSessionRepository {

    private final ConcurrentHashMap<String, UserSession> userBySessionId = new ConcurrentHashMap<>();

    @Override
    public UserSession findBySessionId(String sessionId) {
        return userBySessionId.get(sessionId);
    }

    @Override
    public List<UserSession> getAllSession() {
        return userBySessionId.values()
                .stream()
                .toList();
    }

    @Override
    public UserSession add(String sessionId, UserSession userSession) {
        log.info("register UserSession");
        log.info("Input UserName : {}", userSession.getName());
        userBySessionId.put(sessionId, userSession);
        return userSession;
    }

    @Override
    public boolean remove(String sessionId) {
        if (userBySessionId.containsKey(sessionId)) {
            log.info("removeSession sessionId {}", sessionId);
            userBySessionId.remove(sessionId);
            return true;
        }
        return false;
    }
}