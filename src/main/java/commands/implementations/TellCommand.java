package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

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

            //check if there is a client online with that username
            if(server.isUserOnline(args[0])) {
                ConnectionRequestHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                        .findFirst()
                        .get();

                // put together the message
                StringBuilder msgForClient = new StringBuilder(dl.getMessage("prefix"));
                for(int i = 1; i < args.length; i++) {
                    msgForClient.append(args[i]).append(" ");
                }

                StringBuilder msgForConsole = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    msgForConsole.append(args[i]).append(" ");
                }

                targetUser.sendServerMessage(msgForClient.toString());
                console.print("Sending message to "+args[0]+":"+msgForConsole.toString());
            }else{
                console.print(args[0]+" "+dl.getMessage("offline"));
            }
        }

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // if the try fails -> permission denied
            try {
                SudoSession session = server.getSudoSession(client);
                server.getAllSudoSessions().remove(session);


                if (args.length < 2) {
                    client.sendServerMessage(dl.getMessage("missing-argument"));
                    return;
                }

                //check if there is a client online with that username
                if(server.isUserOnline(args[0])) {
                    ConnectionRequestHandler targetUser = server.getConnectedClients()
                            .stream()
                            .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                            .findFirst()
                            .get();

                    // put together the message
                    StringBuilder msgForClient = new StringBuilder(dl.getMessage("prefix"));
                    for(int i = 1; i < args.length; i++) {
                        msgForClient.append(args[i]).append(" ");
                    }

                    StringBuilder msgForConsole = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        msgForConsole.append(args[i]).append(" ");
                    }

                    targetUser.sendServerMessage(msgForClient.toString());
                    client.sendServerMessage(dl.getMessage("prefix")+"Sending message to "+args[0]+":"+msgForConsole.toString());
                }else{
                    String msg = String.format("%s%s %s",
                            dl.getMessage("prefix"), args[0], dl.getMessage("offline"));
                    client.sendServerMessage(msg);
                }

            } catch (Exception e) {
                client.sendServerMessage(dl.getMessage("perm-denied"));
            }
        }
    }
}
