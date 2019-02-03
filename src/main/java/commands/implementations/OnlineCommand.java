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

public class OnlineCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);


        if (sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            int users = server.getConnectedClients().size();

            StringBuilder usersText = new StringBuilder("~1~dOnline users ("+users+"):~g");
            for(ConnectionRequestHandler cl : server.getConnectedClients()) {
                String entry = String.format("[%s] %s, ",
                        cl.getClientData().getActiveChannel().getName(), cl.getClientData().getUsername());
                usersText.append(entry);
            }
            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", usersText.toString());
            client.sendRequest(new Request(RequestType.ACTION, payload));
            return;
        }

        if(sender instanceof Console) {
            int users = server.getConnectedClients().size();
            StringBuilder usersText = new StringBuilder("Online users ("+users+"):");
            for(ConnectionRequestHandler cl : server.getConnectedClients()) {
                String entry = String.format("[%s] %s, ",
                        cl.getClientData().getActiveChannel().getName(), cl.getClientData().getUsername());
                usersText.append(entry);
            }

            server.getConsole().print(usersText.toString());
        }
    }
}
