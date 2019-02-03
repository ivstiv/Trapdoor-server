package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;

public class StopCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof Console) {
            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            server.stopServer();
        }
    }
}
