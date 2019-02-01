package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.ServiceLocator;
import data.DataLoader;

public class UnblockCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof ConnectionRequestHandler) {

            DataLoader dl = ServiceLocator.getService(DataLoader.class);

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if arguments exists
            if (args.length < 1) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to unblock himself
            if (client.getClientData().getUsername().equals(args[0])) {
                client.sendServerMessage(dl.getMessage("cant-unblock"));
                return;
            }

            // check if the user is blocked
            if(client.getClientData().getBlockedUsernames().contains(args[0])) {

                // block the username
                client.getClientData().unblockUsername(args[0]);

                String msg = String.format("%s%s %s",
                        dl.getMessage("prefix"), args[0], dl.getMessage("success-unblocked"));
                client.sendServerMessage(msg);

            }else{
                client.sendServerMessage(dl.getMessage("already-unblocked"));
            }
        }
    }
}
