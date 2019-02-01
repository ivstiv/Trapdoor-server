package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import communication.Request;
import communication.RequestType;

public class ClearCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof ConnectionRequestHandler) {

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "clear-chat");
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
        }
    }
}
