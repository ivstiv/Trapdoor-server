package communication;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import commands.CommandSender;
import core.Config;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ChannelType;
import exceptions.MalformedRequestException;

import java.net.Socket;

public class ConnectionRequestHandler extends ConnectionHandler implements CommandSender {

    public ConnectionRequestHandler(Socket client) {
        super(client);
    }

    @Override
    public void run() {
        System.out.println("Starting Handler thread:"+this.toString());
        initialiseStreams();

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
        while (true) {
            Request r = null;
            try {
                r = readRequest();
            } catch (MalformedRequestException e) {
                e.printStackTrace();
                continue; // skip the iteration if the request is invalid
            }

            // send confirmation if it is not disconnect request
            JsonObject content = new JsonObject();
            content.addProperty("code", 100);
            content.addProperty("timestamp", r.getTimestamp());
            Request confirmation = new Request(RequestType.RESPONSE, content);
            sendRequest(confirmation);

            switch(r.getType()) {
                case MSG:
                    String message = r.getContent().get("message").getAsString();

                    if(message.startsWith("/")) {
                        // dispatch the command
                        String name = message.split(" ")[0].replaceAll("/", "");
                        String[] args = null;
                        if(message.length() > name.length()+1) {
                            args = message.substring(name.length()+2).split(" ");
                        }else{
                            args = new String[0];
                        }
                        server.dispatchCommand(this, name, args);
                    }else{
                        getActiveChannel().broadcastMsg(this, message); // forward the message
                    }
                    break;
                case ACTION:
                    String action = r.getContent().get("action").getAsString();
                    break;
                case DISCONNECT:
                    // may be close the streams first
                    System.out.println("Stopping Handler thread:"+this.toString());
                    server.removeConnectedClient(this);
                    return;
                case CONNECT:
                    String username = r.getContent().get("username").getAsString();
                    String password = r.getContent().get("password").getAsString();

                    // check if the server is full
                    if(server.getConnectedClients().size() >= Config.getInt("slots")) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 203);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        break;
                    }

                    //server password check
                    if(!Config.getString("password").isEmpty()) {
                        if(!Config.getString("password").equals(password)) {
                            JsonObject payload = new JsonObject();
                            payload.addProperty("code", 200);
                            Request req = new Request(RequestType.RESPONSE, payload);
                            sendRequest(req);
                            break;
                        }
                    }

                    // forbidden username check
                    if(Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(username))) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 202);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        break;
                    }

                    // existing username check
                    if(server.isUserOnline(username)) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 201);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        break;
                    }

                    // send motd
                    JsonObject payload = new JsonObject();
                    payload.addProperty("action", "print");
                    payload.addProperty("message", Config.getString("motd"));
                    Request motd = new Request(RequestType.ACTION, payload);
                    sendRequest(motd);

                    // setup the user's channel
                    this.username = username;
                    this.activeChannel = server.getChannel(ChannelType.DEFAULT);
                    activeChannel.addClient(this);
                    server.addConnectedClient(this);
                    String msg = String.format("%s%s %s",
                            dl.getMessage("prefix"), getUsername(), dl.getMessage("join-server"));
                    server.getChannel(ChannelType.DEFAULT).broadcastPrint(msg);

                    // update status bar
                    payload = new JsonObject();
                    payload.addProperty("action", "update_statusbar");
                    payload.addProperty("channel", activeChannel.getName());
                    Request statusBar = new Request(RequestType.ACTION, payload);
                    sendRequest(statusBar);
                    break;
            }
        }
    }
}
