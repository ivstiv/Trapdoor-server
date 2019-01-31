package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import core.ServiceLocator;
import data.DataLoader;

public class HelpCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof RequestHandler) {
            DataLoader dl = ServiceLocator.getService(DataLoader.class);
            RequestHandler client = (RequestHandler) sender;

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", dl.getMessage("help"));
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
        }
    }
}
