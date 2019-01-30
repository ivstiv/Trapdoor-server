package commands;

import com.google.gson.JsonObject;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import core.ServerWrapper;
import core.ServiceLocator;

public class OnlineCommand implements CommandExecutor{
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof RequestHandler) {

            RequestHandler client = (RequestHandler) sender;

            int players = ServiceLocator.getService(ServerWrapper.class).connectedClients();

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", players);
            client.sendRequest(new Request(RequestType.ACTION, payload));
        }
    }
}
