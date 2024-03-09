package cit.camistrick.handler;

import cit.camistrick.action.KurentoAction;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KurentoActionResolver {
    private static final String NOT_MATCH_ID = "error";
    private static final String CLOSE_ID = "exit";
    private final Map<String, KurentoAction> handlerMap;

    public KurentoAction findAction(String id) {
        return handlerMap.getOrDefault(id, handlerMap.get(NOT_MATCH_ID));
    }

    public KurentoAction getCloseAction() {
        return findAction(CLOSE_ID);
    }
}