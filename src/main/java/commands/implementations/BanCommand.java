package commands.implementations;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
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

            String username = args[0];

            // add the username to forbidden usernames
            if(!Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(username))) {
                // add the name to the list
                Config.getJsonArray("forbidden_usernames").add(username);
                Config.updateFile();

                //check if there is a client online with that username and kick it
                if(server.isUserOnline(args[0])) {
                    ConnectionRequestHandler targetUser = server.getConnectedClients()
                            .stream()
                            .filter(cl -> cl.getClientData().getUsername().equals(username))
                            .findFirst()
                            .get();

                    targetUser.sendServerErrorMessage(dl.getMessage("banned"));
                    targetUser.stopConnection();
                }

                console.print(username+" "+dl.getMessage("cl-banned"));
            }else{
                console.print(username+" "+dl.getMessage("cl-already-banned"));
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
            if(args.length < 1) {
                client.sendServerErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            String username = args[0];

            // add the username to forbidden usernames
            if(!Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(username))) {
                // add the name to the list
                Config.getJsonArray("forbidden_usernames").add(username);
                Config.updateFile();

                //check if there is a client online with that username and kick it
                if(server.isUserOnline(args[0])) {
                    ConnectionRequestHandler targetUser = server.getConnectedClients()
                            .stream()
                            .filter(cl -> cl.getClientData().getUsername().equals(username))
                            .findFirst()
                            .get();

                    targetUser.sendServerErrorMessage(dl.getMessage("banned"));
                    targetUser.stopConnection();
                }

                client.sendServerMessage(username+" "+dl.getMessage("cl-banned"));
            }else{
                client.sendServerErrorMessage(username+" "+dl.getMessage("cl-already-banned"));
            }
        }
    }
}
