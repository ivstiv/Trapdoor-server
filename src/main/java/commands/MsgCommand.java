package commands;

import com.google.gson.JsonObject;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import core.ServiceLocator;
import data.DataLoader;

public class MsgCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof RequestHandler) {

            DataLoader dl = ServiceLocator.getService(DataLoader.class);
            RequestHandler client = (RequestHandler) sender;

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", dl.getMessage("not-implemented"));
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
        }
    }
}
