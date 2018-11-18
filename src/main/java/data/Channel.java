package data;

import com.google.gson.JsonObject;
import communication.AbstractHandler;

import java.util.HashSet;
import java.util.Set;

public class Channel {

    private final ChannelType type;
    private final String name, password;
    private final Set<AbstractHandler> clients = new HashSet<>();

    public Channel(ChannelType type, String name, String password) {
        this.type = type;
        this.name = name;
        this.password = password;
    }

    public void addClient(AbstractHandler client) {
        clients.add(client);
    }

    public void removeClient(String username) {
        for(AbstractHandler client : clients) {
            if(client.getUsername().equals(username)) {
                clients.remove(client);
                return;
            }
        }
        System.out.println("[WARNING]Trying to remove a non-existing handler from channel:"+username);
    }

    public void broadcast(String message) {
        for(AbstractHandler client : clients) {
            JsonObject content = new JsonObject();
            content.addProperty("msg", message);
            Request req = new Request(RequestType.MSG, content);
            client.sendRequest(req);
        }
    }
}
