package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionHandler;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class StopCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof Console) {
            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            server.stopServer();
        }

        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();
            DataLoader dl = ServiceLocator.getService(DataLoader.class);

            client.sendPrefixedMessage(dl.getMessage("unknown-command"));
        }
    }
}
