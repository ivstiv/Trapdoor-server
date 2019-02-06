package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

import java.util.Arrays;

public class TellCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if(sender instanceof Console) {
            Console console = (Console) sender;

            if (args.length < 2) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            String recipient = args[0];
            String[] msg = Arrays.copyOfRange(args, 1, args.length);

            //check if there is a client online with that username
            if(server.isUserOnline(args[0])) {
                ConnectionRequestHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(recipient))
                        .findFirst()
                        .get();

                // put together the message
                String msgForClient = buildMessage(dl.getMessage("prefix"), msg);
                String msgForConsole = buildMessage("", msg);

                targetUser.sendServerMessage(msgForClient);
                console.print("Sending message to "+args[0]+":"+msgForConsole);
            }else{
                console.print(args[0]+" "+dl.getMessage("offline"));
            }
        }



        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if there is a sudo session
            if(!client.getClientData().hasSudoSession()) {
                client.sendServerMessage(dl.getMessage("invalid-sudo-session"));
                return;
            }

            SudoSession session = client.getClientData().getSudoSession();

            // check if it is authenticated
            if(!session.isAuthenticated()) {
                client.sendServerMessage(dl.getMessage("perm-denied"));
                client.getClientData().destroySudoSession();
                return;
            }

            // check for arguments
            if(args.length < 2) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            String recipient = args[0];
            String[] msg = Arrays.copyOfRange(args, 1, args.length);

            //check if there is a client online with that username
            if(server.isUserOnline(args[0])) {
                ConnectionRequestHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(recipient))
                        .findFirst()
                        .get();

                // put together the message
                String msgForClient = buildMessage(dl.getMessage("prefix"), msg);
                String msgForConsole = buildMessage("", msg);

                targetUser.sendServerMessage(msgForClient);
                client.sendServerMessage(dl.getMessage("prefix")+"Sending message to "+recipient+":"+msgForConsole);
            }else{
                String offlineResponse = String.format("%s%s %s",
                        dl.getMessage("prefix"), args[0], dl.getMessage("offline"));
                client.sendServerMessage(offlineResponse);
            }
        }
    }
}
