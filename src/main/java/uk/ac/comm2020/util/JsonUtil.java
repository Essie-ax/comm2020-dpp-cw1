package uk.ac.comm2020.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

// Small json helper.
// Wrap Gson stuff, so other code look clean.
public final class JsonUtil {
    // one Gson instance is enough
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private JsonUtil() {
    }

    // get Gson if someone need it
    public static Gson gson() {
        return GSON;
    }

    // turn object -> json string
    public static String toJson(Object value) {
        return GSON.toJson(value);
    }

    // parse json string -> JsonObject
    // if input empty, return empty object
    public static JsonObject parseObject(String json) {
        if (json == null || json.isBlank()) {
            return new JsonObject();
        }
        return JsonParser.parseString(json).getAsJsonObject();
    }

    // parse json string -> JsonArray
    // if input empty, return empty array
    public static JsonArray parseArray(String json) {
        if (json == null || json.isBlank()) {
            return new JsonArray();
        }
        return JsonParser.parseString(json).getAsJsonArray();
    }

    // parse json array string -> List<String>
    public static List<String> parseStringList(String json) {
        JsonArray array = parseArray(json);
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return values;
    }
}
