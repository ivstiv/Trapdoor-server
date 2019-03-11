package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionHandler;
import communication.RequestType;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;

public class OnlineCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);


        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();

            int users = server.getConnectedClients().size();

            StringBuilder usersText = new StringBuilder("~1~dOnline users ("+users+"):~g");
            for(ConnectionHandler cl : server.getConnectedClients()) {
                String entry = String.format("[%s] %s, ",
                        cl.getClientData().getActiveChannel().getName(), cl.getClientData().getUsername());
                usersText.append(entry);
            }

            client.sendMessage(usersText.toString());
        }

        if(sender instanceof Console) {
            int users = server.getConnectedClients().size();
            StringBuilder usersText = new StringBuilder("Online users ("+users+"):");
            for(ConnectionHandler cl : server.getConnectedClients()) {
                String entry = String.format("[%s] %s, ",
                        cl.getClientData().getActiveChannel().getName(), cl.getClientData().getUsername());
                usersText.append(entry);
            }

            server.getConsole().print(usersText.toString());
        }
    }
}
