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

import java.util.stream.Collectors;

public class InfoCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if(sender instanceof Console) {
            Console console = (Console) sender;

            if(args.length < 1) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            String username = args[0];

            // check if the supplied username is online
            if(server.isUserOnline(username)) {
                // find the connection handler with that username
                ConnectionHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(username))
                        .findFirst()
                        .get();

                String blockedList = targetUser.getClientData().getBlockedUsernames()
                        .stream()
                        .collect(Collectors.joining(", "));

                String msg = String.format(dl.getMessage("cl-user-info"),
                        targetUser.getClientData().getUsername(),
                        targetUser.getClientData().getIp(),
                        targetUser.getClientData().getActiveChannel().getName(),
                        targetUser.getClientData().getBlockedUsernames().size(),
                        blockedList,
                        targetUser.getClientData().isMuted()
                );

                // echo the message
                console.print(msg);
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

            // check if the supplied username is online
            if(server.isUserOnline(username)) {
                // find the connection handler with that username
                ConnectionHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(username))
                        .findFirst()
                        .get();

                String blockedList = targetUser.getClientData().getBlockedUsernames()
                        .stream()
                        .collect(Collectors.joining(", "));

                String msg = String.format(dl.getMessage("user-info"),
                        targetUser.getClientData().getUsername(),
                        targetUser.getClientData().getIp(),
                        targetUser.getClientData().getActiveChannel().getName(),
                        targetUser.getClientData().getBlockedUsernames().size(),
                        blockedList,
                        targetUser.getClientData().isMuted()
                );

                // echo the message
                client.sendPrefixedMessage(msg);

            }else{
                client.sendPrefixedErrorMessage(username+" "+dl.getMessage("offline"));
            }
        }
    }
}
