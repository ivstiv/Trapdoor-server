package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import commands.*;
import commands.implementations.*;
import communication.Request;
import communication.ConnectionRequestHandler;
import communication.RequestType;
import data.Channel;
import data.ChannelType;
import data.Config;
import data.DataLoader;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Predicate;

public class ServerWrapper extends Thread{

    private List<Channel> channels = new ArrayList<>();
    private Set<ConnectionRequestHandler> connectedClients = new HashSet<>();   // holds all connections that have established RSA tunnel
    private Set<ConnectionRequestHandler> preAuthClients = new HashSet<>();     // holds all connections that haven't established RSA tunnel
    private CommandRegister commandRegister = new CommandRegister();
    private Console console = new Console(this);


    public Console getConsole() {
        return console;
    }

    public void dispatchCommand(CommandSender sender, String name, String[] args) {

        Command cmd = new Command(sender, name, args);
        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if(!commandRegister.dispatch(cmd)) {  // return false if the command is not registered
            if(sender instanceof ConnectionRequestHandler) {

                ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

                JsonObject payload = new JsonObject();
                payload.addProperty("action", "print");
                payload.addProperty("message", dl.getMessage("unknown-command"));
                Request req = new Request(RequestType.ACTION, payload);
                client.sendRequest(req);
                return;
            }
            if(sender instanceof  Console) {
                console.print(dl.getMessage("cl-unknown-cmd"));
            }
        }

    }

    public Set<ConnectionRequestHandler> getConnectedClients() {
        return Collections.unmodifiableSet(connectedClients);
    }

    public void addConnectedClient(ConnectionRequestHandler r) {
        connectedClients.add(r);
        preAuthClients.remove(r);
    }
    public void removeConnectedClient(ConnectionRequestHandler r) {
        connectedClients.remove(r);
        if(r.getClientData().getActiveChannel() != null)
            r.getClientData().getActiveChannel().removeClient(r.getClientData().getUsername());
    }

    public boolean isUserOnline(String username) {
        Predicate<ConnectionRequestHandler> filter = handler -> handler.getClientData().getUsername().equals(username);
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

    private void load() {
        console.print("Loading config data. . .");
        // load channels from config
        JsonArray arr = Config.getJsonArray("channels");
        if(arr.size() == 0) {
            console.printError("There aren't any defined channels in the config!");
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
        commandRegister.registerCommand("r", new RespondCommand());
        commandRegister.registerCommand("clear", new ClearCommand());
        commandRegister.registerCommand("block", new BlockCommand());
        commandRegister.registerCommand("unblock", new UnblockCommand());

        for(JsonElement c : Config.getJsonArray("forbidden_commands")) {
            console.print("Command /"+c.getAsString()+" was disabled!");
            commandRegister.unregisterCommand(c.getAsString());
        }

        console.start();
    }

    @Override
    public void run() {
        load();
        try {
            ServerSocket ss = new ServerSocket(Config.getInt("port"));    // opening the socket

            console.print("Connection receiver running on port:"+Config.getInt("port"));
            Socket socket;
            ConnectionRequestHandler client;
            while (true) {
                try {
                    socket = ss.accept();
                }catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                client = new ConnectionRequestHandler(socket);
                client.start();
                preAuthClients.add(client);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

