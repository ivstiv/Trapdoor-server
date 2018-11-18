package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import communication.RequestHandler;
import data.Channel;
import data.ChannelType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Predicate;

public class ServerWrapper {

    private List<Channel> channels = new ArrayList<>();
    private Set<RequestHandler> connectedClients = new HashSet<>();

    public void addConnectedClient(RequestHandler r) {
        connectedClients.add(r);
    }

    public boolean isUserOnline(String username) {
        Predicate<RequestHandler> filter = handler -> handler.getUsername().equals(username);
        return connectedClients.stream().anyMatch(filter);
    }

    public Channel getChannel(ChannelType type) {
        for(Channel ch : channels) {
            if(ch.getType().equals(type))
                return ch;
        }
        return null;
    }

    public Channel getChannel(String name) {
        for(Channel ch : channels) {
            if(ch.getName().equals(name))
                return ch;
        }
        return null;
    }

    public void load() {
        // load channels from config
        JsonArray arr = Config.getJsonArray("channels");
        if(arr.size() == 0) {
            System.out.println("[ERROR]There aren't any defined channels in the config!");
            System.exit(1);
        }
        for(JsonElement el : arr) {
            ChannelType type = ChannelType.valueOf(el.getAsJsonObject().get("type").getAsString().toUpperCase());
            String name = el.getAsJsonObject().get("name").getAsString();
            if(type == ChannelType.PRIVATE) {
                String password = el.getAsJsonObject().get("password").getAsString();
                channels.add(new Channel(type, name, password));
            }else{
                channels.add(new Channel(type, name, null));
            }
        }
    }

    public void start() {

        load();

        Thread requestReceiver = new Thread() {
            public void run() {
                try {
                    ServerSocket ss = new ServerSocket(Config.getInt("port"));    // opening the socket

                    System.out.printf("Request receiver running at port %d . . .\n", Config.getInt("port"));
                    Socket socket;
                    RequestHandler client;
                    while (true) {
                        try {
                            socket = ss.accept();
                        }catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                        client = new RequestHandler(socket);
                        client.start();
                        System.out.println("Connected client");
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        requestReceiver.start();
    }
}

