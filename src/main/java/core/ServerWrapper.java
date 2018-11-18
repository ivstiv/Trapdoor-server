package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import communication.RequestHandler;
import data.Channel;
import data.ChannelType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWrapper {

    private Channel[] channels;
    private RequestHandler[] connectionHandlers;

    public void load() {
        // load channels from config
        JsonArray arr = Config.getJsonArray("channels");
        Channel[] channels = new Channel[arr.size()];
        for(int i = 0; i < channels.length; i++) {
            ChannelType type = ChannelType.valueOf(arr.get(i).getAsJsonObject().get("type").getAsString());
            String name = arr.get(i).getAsJsonObject().get("name").getAsString();
            if(type == ChannelType.PRIVATE) {
                String password = arr.get(i).getAsJsonObject().get("password").getAsString();
                channels[i] = new Channel(type, name, password);
            }else{
                channels[i] = new Channel(type, name, null);
            }
        }
    }

    public void start() {

        Thread requestReceiver = new Thread() {
            public void run() {

                ServerSocket ss = null;

                try {
                    ss = new ServerSocket(Config.getInt("port"));    // opening the socket
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.printf("Request receiver running at port %d . . .\n", Config.getInt("port"));
                Socket socket;
                RequestHandler client;
                try {
                    while (true) {
                        socket = ss.accept();
                        client = new RequestHandler(socket);
                        client.start();
                        System.out.println("Connected client");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        requestReceiver.start();
    }
}
}
