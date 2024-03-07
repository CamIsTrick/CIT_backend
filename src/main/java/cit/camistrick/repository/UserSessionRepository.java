package cit.camistrick.repository;

import cit.camistrick.domain.UserSession;

import java.util.List;

public interface UserSessionRepository {
    UserSession findBySessionId(String sessionId);

    List<UserSession> getAllSession();

    UserSession add(String sessionId, UserSession userSession);

    boolean remove(String sessionId);
}