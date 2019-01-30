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
            String msg = "~1~dOnline players ("+players+"):";
            // TODO: 30-Jan-19 may be list all online players once /join channel works
            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", msg);
            client.sendRequest(new Request(RequestType.ACTION, payload));
            // TODO: 30-Jan-19 add option for console sender later
        }
    }
}
