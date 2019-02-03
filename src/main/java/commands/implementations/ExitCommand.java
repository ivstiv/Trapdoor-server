package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionRequestHandler;
import communication.RequestType;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class ExitCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if(sender instanceof ConnectionRequestHandler) {

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", dl.getMessage("bye"));
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);

            JsonObject payload2 = new JsonObject();
            payload2.addProperty("action", "reset_statusbar");
            Request response2 = new Request(RequestType.ACTION, payload2);
            client.sendRequest(response2);

            // broadcast that the client has disconnected
            String msg = String.format("%s%s %s",
                    dl.getMessage("prefix"), client.getClientData().getUsername(), dl.getMessage("left-server"));
            client.getClientData().getActiveChannel().broadcastPrint(msg);

            // remove the client and shut down the thread and the socket streams
            client.stopConnection();
        }

        if(sender instanceof Console) {
            server.getConsole().print(dl.getMessage("cl-unknown-cmd"));
        }
    }
}
