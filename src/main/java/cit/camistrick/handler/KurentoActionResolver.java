package cit.camistrick.handler;

import cit.camistrick.Action.KurentoAction;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class KurentoActionResolver {
    private static final String NOT_MATCH_ID = "error";
    private static final String CLOSE_ID = "exit";
    private final Map<String, KurentoAction> handlerMap;

    public KurentoActionResolver(Map<String, KurentoAction> handlerMap) {
        this.handlerMap = Optional.ofNullable(handlerMap).orElse(Collections.emptyMap());
    }

    public KurentoAction findAction(String id) {
        return handlerMap.getOrDefault(id, handlerMap.get(NOT_MATCH_ID));
    }


    public KurentoAction getCloseAction() {
        return findAction(CLOSE_ID);
    }
}