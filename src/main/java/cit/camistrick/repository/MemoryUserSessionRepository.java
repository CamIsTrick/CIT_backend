package cit.camistrick.repository;

import cit.camistrick.domain.Room;
import cit.camistrick.domain.UserSession;
import cit.camistrick.service.RoomManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MemoryUserSessionRepository implements UserSessionRepository {
    private final ConcurrentHashMap<String, UserSession> userBySessionId = new ConcurrentHashMap<>();
    private final RoomManager roomManager = new RoomManager();

    @Override
    public UserSession findBySessionId(String sessionId) {
        return userBySessionId.get(sessionId);
    }

    public List<UserSession> findAllByRoomId(String roomId) {
        Room room = roomManager.getRoom(roomId);
        if (room != null) {
            return room.getAllUserSessions();
        }
        return Collections.emptyList();
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