package data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.function.Predicate;

public class Config {

    private static JsonObject properties = new JsonObject();
    private static File config = new File("config.json");
    private static Gson gson = new Gson();

    private Config() {}

    public static JsonElement getProperty(String key) {
        init();
        return properties.get(key);
    }

    public static int getInt(String key) {
        init();
        return properties.get(key).getAsInt();
    }

    public static String getString(String key) {
        init();
        return properties.get(key).getAsString();
    }

    public static JsonArray getJsonArray(String key) {
        init();
        return properties.getAsJsonArray(key);
    }

    public void update() {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(config));
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR]Could not update config!");
            e.printStackTrace();
            return;
        }
        BufferedReader br = new BufferedReader(isr);
        // filter out the comments
        Predicate<String> filter = line -> !line.trim().startsWith("#");
        String cleanJson = br.lines()
                .filter(filter)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        properties = gson.fromJson(cleanJson, JsonObject.class);
    }

    private static void init() {
        if(!isExported())
            exportDefault();

        if(properties.size() == 0) {
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(new FileInputStream(config));
            } catch (FileNotFoundException e) {
                System.out.println("[ERROR]Could not initialise config!");
                e.printStackTrace();
            }
            BufferedReader br = new BufferedReader(isr);
            // filter out the comments
            Predicate<String> filter = line -> !line.trim().startsWith("#");
            String cleanJson = br.lines()
                    .filter(filter)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
            properties = gson.fromJson(cleanJson, JsonObject.class);
        }
    }

    private static void exportDefault() {
        try {
            Files.copy(Config.class.getResourceAsStream("/config.json"), FileSystems.getDefault().getPath("config.json"));
        } catch (IOException e) {
            System.out.println("[ERROR]Could not export default config!");
            e.printStackTrace();
        }
    }

    private static boolean isExported() {
        if(config.exists())
            return true;
        return false;
    }

}
