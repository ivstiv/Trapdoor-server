package commands.implementations;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Config;
import data.DataLoader;

public class BanCommand implements CommandExecutor {
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

            // add the username to forbidden usernames
            if(!Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(args[0]))) {
                // add the name to the list
                Config.getJsonArray("forbidden_usernames").add(args[0]);
                Config.updateFile();

                //check if there is a client online with that username and kick it
                if(server.isUserOnline(args[0])) {
                    ConnectionRequestHandler targetUser = server.getConnectedClients()
                            .stream()
                            .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                            .findFirst()
                            .get();

                    targetUser.sendServerMessage(dl.getMessage("banned"));
                    targetUser.stopConnection();
                }

                console.print(args[0]+" "+dl.getMessage("cl-banned"));
                return;

            }else{
                console.print(args[0]+" "+dl.getMessage("cl-already-banned"));
                return;
            }

        }

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage("NEEDS SUDO IMPLEMENTATION");
        }
    }
}
