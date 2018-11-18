package communication;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import core.Config;
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
        while (true) {
            Request r = null;
            try {
                r = readRequest();
            } catch (MalformedRequestException e) {
                e.printStackTrace();
                continue; // skip the iteration if the request is invalid
            }

            // TODO: 18-Nov-18 send confirmation
            //sendConfirmation(r.getTime()); // sends a confirmation to the client that the request have been received

            switch(r.getType()) {
                case DISCONNECT:
                    // may be close the streams first
                    return;
                case CONNECT:
                    String username = r.getContent().get("username").getAsString();
                    String password = r.getContent().get("password").getAsString();
                    if(!Config.getString("password").equals(password)) {
                        JsonObject content = new JsonObject();
                        content.addProperty("code", 200);
                        Request req = new Request(RequestType.RESPONSE, content);
                        sendRequest(req);
                        break;
                    }
                    // TODO: 18-Nov-18 check if the usernames already exists and return code 201
                    if(Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(username))) {
                        JsonObject content = new JsonObject();
                        content.addProperty("code", 202);
                        Request req = new Request(RequestType.RESPONSE, content);
                        sendRequest(req);
                        break;
                    }
                    // TODO: 18-Nov-18 create a user object and assign a default channel
                    break;
            }
        }
    }
}
