package communication;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import core.Config;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ChannelType;
import data.Request;
import data.RequestType;
import exceptions.MalformedRequestException;

import java.net.Socket;

public class RequestHandler extends AbstractHandler {

    public RequestHandler(Socket client) {
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
                    getActiveChannel().broadcast(r.getContent().get("msg").getAsString());
                    break;
                case ACTION:
                    String action = r.getContent().get("action").getAsString();
                    if(action.equals("change-channel")) {
                        // TODO: 18-Nov-18 service locator needed here to access the server wrapper
                        // TODO: 18-Nov-18 setup the appropriate channels
                    }
                    break;
                case DISCONNECT:
                    // may be close the streams first
                    System.out.println("Stopping Handler thread:"+this.toString());
                    server.removeConnectedClient(this);
                    return;
                case CONNECT:
                    String username = r.getContent().get("username").getAsString();
                    String password = r.getContent().get("password").getAsString();

                    //server password check
                    if(!Config.getString("password").equals(password)) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 200);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        break;
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

                    // setup the user's channel and send motd
                    this.username = username;
                    this.activeChannel = server.getChannel(ChannelType.DEFAULT);
                    activeChannel.addClient(this);
                    server.addConnectedClient(this);
                    // send motd
                    JsonObject payload = new JsonObject();
                    payload.addProperty("action", "show_motd");
                    payload.addProperty("message", Config.getString("motd"));
                    Request motd = new Request(RequestType.ACTION, payload);
                    sendRequest(motd);
                    // update status bar
                    payload = new JsonObject();
                    payload.addProperty("action", "update_statusbar");
                    payload.addProperty("channel", activeChannel.getName());
                    Request statusBar = new Request(RequestType.ACTION, payload);
                    sendRequest(statusBar);
                    // TODO: 18-Nov-18 send available commands when they got implemented :D
                    break;
            }
        }
    }
}
