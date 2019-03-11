package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionHandler;
import communication.Request;
import communication.RequestType;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ANSI;

public class ClearCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();

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
