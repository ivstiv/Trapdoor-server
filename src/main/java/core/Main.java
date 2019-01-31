package core;

import com.google.gson.JsonObject;

public class Main {
    public static void main(String[] args) {


        ServerWrapper server = new ServerWrapper();
        ServiceLocator.initialiseService(server);
        server.start();


    }
}
