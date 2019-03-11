package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionHandler;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class MuteCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if(sender instanceof Console) {
            Console console = (Console) sender;

            if (args.length < 1) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            String username = args[0];

            //check if there is a client online with that username and mute it
            if(server.isUserOnline(username)) {
                ConnectionHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                        .findFirst()
                        .get();


                // check if the user is already muted
                if(targetUser.getClientData().isMuted()) {
                    // already muted
                    console.print(username+" "+dl.getMessage("cl-already-muted"));
                }else{
                    // mute the user
                    targetUser.sendPrefixedErrorMessage(dl.getMessage("muted"));
                    targetUser.getClientData().setMuted(true);
                    console.print(username+" "+dl.getMessage("cl-muted"));
                }

            }else{
                console.print(username+" "+dl.getMessage("offline"));
            }
        }

        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();

            // check if there is a sudo session
            if(!client.getClientData().hasSudoSession()) {
                client.sendPrefixedErrorMessage(dl.getMessage("perm-denied"));
                return;
            }

            SudoSession session = client.getClientData().getSudoSession();

            // check if it is authenticated
            if(!session.isAuthenticated()) {
                client.sendPrefixedErrorMessage(dl.getMessage("perm-denied"));
                client.getClientData().destroySudoSession();
                return;
            }

            // check for arguments
            if(args.length < 1) {
                client.sendPrefixedErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            String username = args[0];

            //check if there is a client online with that username and mute it
            if(server.isUserOnline(username)) {
                ConnectionHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(username))
                        .findFirst()
                        .get();


                // check if the user is already muted
                if(targetUser.getClientData().isMuted()) {
                    // already muted
                    client.sendPrefixedErrorMessage(username+" "+dl.getMessage("already-muted"));
                }else{
                    // mute the user
                    targetUser.sendPrefixedMessage(dl.getMessage("muted"));
                    targetUser.getClientData().setMuted(true);
                    client.sendPrefixedMessage(username+" "+dl.getMessage("muted-confirm"));
                }

            }else{
                client.sendPrefixedMessage(username+" "+dl.getMessage("offline"));
            }
        }
    }
}
