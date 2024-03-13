package cit.camistrick.handler;

import cit.camistrick.action.KurentoAction;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class KurentoActionResolver {
    private static final String NOT_MATCH_ID = "error";
    private static final String CLOSE_ID = "exit";
    private final Map<String, KurentoAction> handlerMap;

    public KurentoAction findAction(JsonObject jsonMessage) {
        String id = findKurentoActionById(jsonMessage);

        if (handlerMap.get(id) == null) {
            return handlerMap.get(NOT_MATCH_ID);
        }
        return handlerMap.get(id);
    }

    @NotNull
    private String findKurentoActionById(JsonObject jsonMessage) {
        return Optional
                .ofNullable(jsonMessage.get("id"))
                .map(JsonElement::getAsString).
                orElse("error");
    }

    public KurentoAction getCloseAction() {
        return handlerMap.get(CLOSE_ID);
    }
}