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
import data.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Predicate;

public class ServerWrapper extends Thread{

    private List<Channel> channels;
    private Set<ConnectionRequestHandler> connectedClients = new HashSet<>();   // holds all connections that have established RSA tunnel
    private Set<ConnectionRequestHandler> preAuthClients = new HashSet<>();     // holds all connections that haven't established RSA tunnel
    private CommandRegister commandRegister;
    private Console console = new Console(this);
    private volatile boolean running = true;
    private ServerSocket ss;


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

        channels = new ArrayList<>();
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

        commandRegister = new CommandRegister();
        // user commands
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
        // admin commands
        commandRegister.registerCommand("info", new InfoCommand());
        commandRegister.registerCommand("ban", new BanCommand());
        commandRegister.registerCommand("unban", new UnbanCommand());
        commandRegister.registerCommand("ipban", new IpbanCommand());
        commandRegister.registerCommand("ipunban", new IpunbanCommand());
        commandRegister.registerCommand("kick", new KickCommand());
        commandRegister.registerCommand("mute", new MuteCommand());
        commandRegister.registerCommand("unmute", new UnmuteCommand());
        commandRegister.registerCommand("broadcast", new BroadcastCommand());
        commandRegister.registerCommand("tell", new TellCommand());
        // console commands
        commandRegister.registerCommand("stop", new StopCommand());
        commandRegister.registerCommand("colors", new ColorsCommand());

        for(JsonElement c : Config.getJsonArray("forbidden_commands")) {
            String cmd = c.getAsString().replaceAll("/","");
            if(commandRegister.registeredCommands().contains(cmd)) {
                console.print("Command /"+cmd+" was disabled!");
                commandRegister.unregisterCommand(cmd);
            }
        }

        console.start();
    }

    @Override
    public void run() {
        load();
        try {
            ss = new ServerSocket(Config.getInt("port"));    // opening the socket

            console.print("Connection receiver running on port:"+Config.getInt("port"));
            Socket socket;
            ConnectionRequestHandler client;
            while (running) {
                try {
                    socket = ss.accept();
                }catch (Exception e) {
                    //e.printStackTrace();
                    break;
                }

                // check if the ip is not banned
                String ip = socket.getInetAddress().getHostAddress();
                if(Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(ip))) {

                    // this will not return anything to the client, just close the socket
                    if(getConsole().getMode().equals("default"))
                        getConsole().print(ANSI.CYAN+"Client tried to login with forbidden ip: "+ip);
                    socket.close();

                } else {

                    client = new ConnectionRequestHandler(socket);
                    client.start();
                    preAuthClients.add(client);

                }

                if(getConsole().getMode().equals("traffic"))
                    getConsole().print("CONNECT FROM: "+ip);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        console.print("Stopping main server thread..");
        this.running = false;
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        console.print("Stopping pre-auth connections..");
        for(ConnectionRequestHandler c : preAuthClients) {
            c.stopConnection();
        }
        console.print("Disconnecting connected clients..");
        for(ConnectionRequestHandler c : connectedClients) {
            c.stopConnection();
        }
        console.print("Stopping command listeners..");
        commandRegister.stopListeners();

        console.print("Stopping terminal interface..");
        console.print("press Enter to continue..");
        console.stopConsole();
    }
}

