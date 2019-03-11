package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionHandler;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class UnblockCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();

            // check if arguments exists
            if (args.length < 1) {
                client.sendPrefixedErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to unblock himself
            if (client.getClientData().getUsername().equals(args[0])) {
                client.sendPrefixedErrorMessage(dl.getMessage("cant-unblock"));
                return;
            }

            // check if the user is blocked
            if(client.getClientData().getBlockedUsernames().contains(args[0])) {

                // block the username
                client.getClientData().unblockUsername(args[0]);
                client.sendPrefixedMessage(args[0]+" "+dl.getMessage("success-unblocked"));

            }else{
                client.sendPrefixedErrorMessage(dl.getMessage("already-unblocked"));
            }
        }

        if(sender instanceof Console) {
            server.getConsole().print(dl.getMessage("cl-unknown-cmd"));
        }
    }
}
