package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class UnblockCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if (sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if arguments exists
            if (args.length < 1) {
                client.sendServerErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to unblock himself
            if (client.getClientData().getUsername().equals(args[0])) {
                client.sendServerErrorMessage(dl.getMessage("cant-unblock"));
                return;
            }

            // check if the user is blocked
            if(client.getClientData().getBlockedUsernames().contains(args[0])) {

                // block the username
                client.getClientData().unblockUsername(args[0]);
                client.sendServerMessage(args[0]+" "+dl.getMessage("success-unblocked"));

            }else{
                client.sendServerErrorMessage(dl.getMessage("already-unblocked"));
            }
        }

        if(sender instanceof Console) {
            server.getConsole().print(dl.getMessage("cl-unknown-cmd"));
        }
    }
}
