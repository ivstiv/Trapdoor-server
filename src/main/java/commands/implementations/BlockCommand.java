package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class BlockCommand implements CommandExecutor {


    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof ConnectionRequestHandler) {

            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            DataLoader dl = ServiceLocator.getService(DataLoader.class);

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if arguments exists
            if(args.length < 1) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to block himself
            if(client.getClientData().getUsername().equals(args[0])) {
                client.sendServerMessage(dl.getMessage("cant-block"));
                return;
            }

            // check if the user to be blocked is online
            if(server.isUserOnline(args[0])) {

                // check if the user is already blocked
                if(!client.getClientData().getBlockedUsernames().contains(args[0])) {

                    // block the username
                    client.getClientData().blockUsername(args[0]);

                    String msg = String.format("%s%s %s",
                            dl.getMessage("prefix"), args[0], dl.getMessage("success-blocked"));
                    client.sendServerMessage(msg);
                }else{
                    client.sendServerMessage(dl.getMessage("already-blocked"));
                }
            }else{
                String msg = String.format("%s%s %s",
                        dl.getMessage("prefix"), args[0], dl.getMessage("offline"));
                client.sendServerMessage(msg);
            }
        }
    }
}
