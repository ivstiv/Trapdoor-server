package communication.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import commands.CommandSender;
import communication.ConnectionHandler;
import communication.Request;
import communication.RequestType;
import data.ANSI;
import data.ChannelType;
import data.Config;

public class ConnectRequestHandler extends RequestHandler implements CommandSender {

    ConnectRequestHandler(ConnectionHandler client) {
        super(client);
    }

    @Override
    public void handle(Request r) {
        String username = r.getContent().get("username").getAsString();
        String password = r.getContent().get("password").getAsString();

        // TODO: 04-Feb-19 check if the username is a-zA-Z0-9 regex

        // check if the server is full
        if(server.getConnectedClients().size() >= Config.getInt("slots")) {
            JsonObject payload = new JsonObject();
            payload.addProperty("code", 203);
            Request req = new Request(RequestType.RESPONSE, payload);
            client.sendRequest(req);
            return;
        }

        //server password check
        if(!Config.getString("password").isEmpty()) {
            if(!Config.getString("password").equals(password)) {
                JsonObject payload = new JsonObject();
                payload.addProperty("code", 200);
                Request req = new Request(RequestType.RESPONSE, payload);
                client.sendRequest(req);
                if(server.getConsole().getMode().equals("default"))
                    server.getConsole().print(ANSI.CYAN+"Client tried to login with wrong password: "+username);
                return;
            }
        }

        // forbidden username check
        if(Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(username))) {
            JsonObject payload = new JsonObject();
            payload.addProperty("code", 202);
            Request req = new Request(RequestType.RESPONSE, payload);
            client.sendRequest(req);
            if(server.getConsole().getMode().equals("default"))
                server.getConsole().print(ANSI.CYAN+"Client tried to login with forbidden username: "+username);
            return;
        }

        // existing username check
        if(server.isUserOnline(username)) {
            JsonObject payload = new JsonObject();
            payload.addProperty("code", 201);
            Request req = new Request(RequestType.RESPONSE, payload);
            client.sendRequest(req);
            return;
        }

        // send motd
        JsonObject payload = new JsonObject();
        payload.addProperty("action", "print");
        payload.addProperty("message", Config.getString("motd"));
        Request motd = new Request(RequestType.ACTION, payload);
        client.sendRequest(motd);

        // setup the user's channel
        clientData.setUsername(username);
        clientData.setActiveChannel(server.getChannel(ChannelType.DEFAULT));
        clientData.getActiveChannel().addClient(client);
        server.addConnectedClient(client);
        String msg = String.format("%s%s %s",
                dl.getMessage("prefix"), clientData.getUsername(), dl.getMessage("join-server"));
        server.getChannel(ChannelType.DEFAULT).broadcastPrint(msg);

        // update status bar
        payload = new JsonObject();
        payload.addProperty("action", "update_statusbar");
        payload.addProperty("channel", clientData.getActiveChannel().getName());
        Request statusBar = new Request(RequestType.ACTION, payload);
        client.sendRequest(statusBar);

        // console print
        if(server.getConsole().getMode().equals("default"))
            server.getConsole().print(ANSI.CYAN+clientData.getUsername()+" joined the server.");
    }
}
