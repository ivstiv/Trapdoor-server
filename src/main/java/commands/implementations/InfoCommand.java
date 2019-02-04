package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
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

            // check if the supplied username is online
            if(server.isUserOnline(args[0])) {
                // find the connection handler with that username
                ConnectionRequestHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                        .findFirst()
                        .get();

                String blockedList = targetUser.getClientData().getBlockedUsernames()
                        .stream()
                        .collect(Collectors.joining(", "));

                String msg = String.format("User info (%s):\n  - IP: %s\n  - Channel: %s\n  - Blocked (%d): %s\n  - Muted: %s",
                        targetUser.getClientData().getUsername(),
                        targetUser.getClientData().getIp(),
                        targetUser.getClientData().getActiveChannel().getName(),
                        targetUser.getClientData().getBlockedUsernames().size(),
                        blockedList,
                        "NOT IMPLEMENTED"
                );

                // echo the message
                console.print(msg);
                return;

            }else{
                console.print(args[0]+" "+dl.getMessage("offline"));
            }
        }

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage("NEEDS SUDO IMPLEMENTATION");
        }
    }
}
