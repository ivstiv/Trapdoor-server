package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
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
            return;
        }

        if(sender instanceof ConnectionRequestHandler) {
            DataLoader dl = ServiceLocator.getService(DataLoader.class);
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            client.sendServerMessage(dl.getMessage("unknown-command"));
        }
    }
}
