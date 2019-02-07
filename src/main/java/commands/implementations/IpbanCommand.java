package commands.implementations;

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

import java.util.Optional;

public class IpbanCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if (sender instanceof Console) {
            Console console = (Console) sender;

            if (args.length < 1) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            String ip = args[0];

            // add the ip to forbidden ips
            if (!Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(ip))) {
                // add the name to the list
                Config.getJsonArray("forbidden_ips").add(ip);
                Config.updateFile();

                // check if there is a connected client with that ip and kick it
                Optional<ConnectionRequestHandler> targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getIp().equals(ip))
                        .findFirst();

                // kick the user if online
                if(targetUser.isPresent()) {
                    targetUser.get().sendServerErrorMessage(dl.getMessage("banned"));
                    targetUser.get().stopConnection();
                }

                console.print(ip+" "+dl.getMessage("cl-banned"));
            }else{
                console.print(ip+" "+dl.getMessage("cl-already-banned"));
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

            String ip = args[0];

            // check if the user is not banned
            if (!Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(ip))) {
                // add the ip to the list
                Config.getJsonArray("forbidden_ips").add(ip);
                Config.updateFile();

                // check if there is a connected client with that ip and kick it
                Optional<ConnectionRequestHandler> targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getIp().equals(ip))
                        .findFirst();

                // kick the user if online
                if(targetUser.isPresent()) {
                    targetUser.get().sendServerErrorMessage(dl.getMessage("banned"));
                    targetUser.get().stopConnection();
                }
            }else{
                client.sendServerErrorMessage(ip+" "+dl.getMessage("cl-already-banned"));
            }
        }
    }
}
