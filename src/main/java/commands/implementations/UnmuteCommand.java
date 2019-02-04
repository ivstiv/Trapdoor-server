package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class UnmuteCommand implements CommandExecutor {
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

            //check if there is a client online with that username and mute it
            if(server.isUserOnline(args[0])) {
                ConnectionRequestHandler targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                        .findFirst()
                        .get();


                // check if the user is muted
                if(targetUser.getClientData().isMuted()) {
                    // unmute the user
                    targetUser.sendServerMessage(dl.getMessage("unmuted"));
                    targetUser.getClientData().setMuted(false);
                    console.print(args[0]+" "+dl.getMessage("cl-unmuted"));
                    return;
                }else{
                    console.print(args[0]+" "+dl.getMessage("cl-not-muted"));
                    return;
                }

            }else{
                console.print(args[0]+" "+dl.getMessage("offline"));
                return;
            }
        }

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage("NEEDS SUDO IMPLEMENTATION");
        }
    }
}
