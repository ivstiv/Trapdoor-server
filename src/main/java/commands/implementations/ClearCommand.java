package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import communication.Request;
import communication.RequestType;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ANSI;

public class ClearCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof ConnectionRequestHandler) {

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "clear_chat");
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
        }

        if(sender instanceof Console) {
            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            server.getConsole().print(ANSI.ERASE_SCREEN);
        }
    }
}
