package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import commands.*;
import commands.implementations.*;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import data.Channel;
import data.ChannelType;
import data.DataLoader;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Predicate;

public class ServerWrapper {

    private List<Channel> channels = new ArrayList<>();
    private Set<RequestHandler> connectedClients = new HashSet<>();
    private CommandRegister commandRegister = new CommandRegister();

    public void dispatchCommand(CommandSender sender, String name, String[] args) {
        Command cmd = new Command(sender, name, args);

        if(!commandRegister.dispatch(cmd)) {  // return false if the command is not registered
            if(sender instanceof RequestHandler) {

                RequestHandler client = (RequestHandler) sender;
                DataLoader dl = ServiceLocator.getService(DataLoader.class);

                JsonObject payload = new JsonObject();
                payload.addProperty("action", "print");
                payload.addProperty("message", dl.getMessage("unknown-command"));
                Request req = new Request(RequestType.ACTION, payload);
                client.sendRequest(req);
            }
        }

    }

    public Set<RequestHandler> getConnectedClients() {
        return Collections.unmodifiableSet(connectedClients);
    }

    public void addConnectedClient(RequestHandler r) {
        connectedClients.add(r);
    }
    public void removeConnectedClient(RequestHandler r) {
        connectedClients.remove(r);
        if(r.getActiveChannel() != null)
            r.getActiveChannel().removeClient(r.getUsername());
    }

    public boolean isUserOnline(String username) {
        Predicate<RequestHandler> filter = handler -> handler.getUsername().equals(username);
        return connectedClients.stream().anyMatch(filter);
    }

    public List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
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

        commandRegister.registerCommand("help", new HelpCommand());
        commandRegister.registerCommand("channels", new ChannelsCommand());
        commandRegister.registerCommand("online", new OnlineCommand());
        commandRegister.registerCommand("join", new JoinCommand());
        commandRegister.registerCommand("msg", new MsgCommand());
        commandRegister.registerCommand("exit", new ExitCommand());
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
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        requestReceiver.start();
    }
}

