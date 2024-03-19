package cit.camistrick.dto.kurento;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public abstract class KurentoDto {
    private final Gson gson = new Gson();

    public String toJson(JsonObject jsonObject) {
        return gson.toJson(jsonObject, this.getClass());
    }

    public <T extends KurentoDto> T toDto(JsonObject jsonObject, Class<T> clazz) {
        return gson.fromJson(jsonObject.toString(), clazz);
    }
}