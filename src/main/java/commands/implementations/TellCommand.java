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
            if(server.isUserOnline(recipient)) {
                ConnectionRequestHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(recipient))
                        .findFirst()
                        .get();

                // put together the message
                String msgForClient = buildMessage("", msg);
                String msgForConsole = buildMessage("", msg);

                targetUser.sendServerMessage(msgForClient);
                console.print(dl.getMessage("sending-msg")+recipient+":"+msgForConsole);
            }else{
                console.print(recipient+" "+dl.getMessage("offline"));
            }
        }



        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if there is a sudo session
            if(!client.getClientData().hasSudoSession()) {
                client.sendServerErrorMessage(dl.getMessage("perm-denied"));
                return;
            }

            SudoSession session = client.getClientData().getSudoSession();

            // check if it is authenticated
            if(!session.isAuthenticated()) {
                client.sendServerErrorMessage(dl.getMessage("perm-denied"));
                client.getClientData().destroySudoSession();
                return;
            }

            // check for arguments
            if(args.length < 2) {
                client.sendServerErrorMessage(dl.getMessage("missing-argument"));
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
                String msgForRecipient = buildMessage("", msg);
                String msgForSender = buildMessage("", msg);

                targetUser.sendServerMessage(msgForRecipient);
                client.sendServerMessage(dl.getMessage("sending-msg")+recipient+":"+msgForSender);
            }else{
                client.sendServerErrorMessage(recipient+" "+dl.getMessage("offline"));
            }
        }
    }
}
