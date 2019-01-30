package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import core.Main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataLoader {
    private JsonObject messages;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /*

        MESSAGES

    */
    public String getMessage(String key) {
        if(this.messages == null)
            this.messages = loadMessages();
        if(messages.has(key))
            return messages.get(key).getAsString();
        else
            return "[ERROR] MISSING KEY:"+key;
    }

    private JsonObject loadMessages() {
        InputStream stream = Main.class.getClassLoader().getResourceAsStream("messages.json");
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        String json = br.lines().collect(StringBuilder::new,StringBuilder::append,StringBuilder::append).toString();
        return gson.fromJson(json, JsonObject.class);
    }
}
